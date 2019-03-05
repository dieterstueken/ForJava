package de.dst.fortran.code;

import de.dst.fortran.XmlWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static de.dst.fortran.code.Analyzer.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.04.17
 * Time: 17:21
 */
public class CodeAnalyzer implements CodeElement {

    final Analyzer analyzer;

    public final Code block;

    public final Element be;

    Boolean ready = false;

    String line = "";

    @Override
    public Element getElement() {
        return be;
    }

    private Element createElement(String name) {
        return be.getOwnerDocument().createElement(name);
    }

    @Override
    public String getLine() {
        return line;
    }

    void setLine(String line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return block.name + ":" + line;
    }

    CodeAnalyzer(Analyzer analyzer, Element be) {
        this.analyzer = analyzer;
        this.be = be;
        block = new Code(Analyzer.getPath(be), be);
        be.setAttribute("path", block.path);

        System.out.format("  define %s:%s\n", block.path, block.name);
    }

    public Code code() {

        if (ready == null)
            throw new IllegalStateException("dependeny loop: " + block.name);

        if (!ready) {
            String indent = analyzer.indent;
            System.out.format("%sparse %s:%s\n", indent, block.path, block.name);
            analyzer.indent += "    ";
            ready = null;

            try {
                parse();
            } catch(Throwable e) {
                String message = String.format("error parsing %s.%s:%s", block.path, block.name, line);
                throw new RuntimeException(message, e);
            }

            //System.out.format("%sdone  %s:%s\n", indent, block.path, block.name);
            analyzer.indent = indent;
            ready = true;
        }

        return block;
    }

    private void parse() {

        Analyzer.childElements(be).forEach(ce -> {
            switch(ce.getNodeName()) {

                case "args":
                    args(ce);
                    break;

                case "decl":
                    decl(ce);
                    break;

                case "code":
                    code(ce);
                    break;
            }
        });

        // finally refresh arguments again
        //childElements(be, "args").findFirst().ifPresent(this::prepareArgs);
    }

    //void prepareArgs(Element args) {
    //    variables(args, define(block.arguments::get));
    //}

    private void args(Element args) {
        Analyzer.childElements(args, "arg").stream()
                .flatMap(ce-> Analyzer.childElements(ce, "var").stream())
                .forEach(this::arg);

        block.arguments.size();
    }

    Variable contextVariable(Element e, Function<String, Variable> variables) {
        final String name = e.getAttribute("name");

        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("no name");

        final Variable var = variables.apply(name);
        return setup(e, var);
    }

    boolean isLocal(String name) {
        Variable v = block.variables.find(name);
        return v!=null && v.isLocal();
    }

    Variable blockVariable(Element e) {
         return contextVariable(e, block.variables::get);
    }

    Variable setup(Element e, Variable v) {
        Context context = v.context;

//        if(context instanceof Variable) {
//            Variable a = (Variable) context;
//            e.setAttribute("of", a.getName());
//            int n = a.dim().indexOf(v);
//            if(n>=0)
//                e.setAttribute("dim", Integer.toString(n));
//            context = a.context;
//        }

        if (context instanceof CommonAnalyzer) {
            e.setAttribute("common", context.getName());
        }

        if (context == block)
            if (v.isAssigned())
                e.setAttribute("assigned", "true");

        if (v.type != null) {
            e.setAttribute("type", v.type.toString());
        }

        return v;
    }

    // access variable
    Variable readVariable(Element e) {
        if (e == null)
            return null;

        // possibly define new one
        Variable v = blockVariable(e);

        return v;
    }

    Variable arg(Element e) {
        return contextVariable(e, block.arguments::get);
    }

    private void decl(Element e) {

        Analyzer.childElements(e).forEach(ce -> {
            switch(ce.getNodeName()) {

                case "common":
                    common(ce);
                    break;

                case "dim":
                    Type type = parseType(ce.getAttribute("type"));
                    childElements(ce).forEach(de -> {
                        // plain value
                        if ("var".equals(de.getNodeName())) {
                            blockVariable(de).decl(type);
                        } else if ("arr".equals(de.getNodeName())) {
                            // array or matrix definition
                            array(de, block.variables::get).decl(type);
                        }
                    });
                    break;

                case "F":
                    setLine(ce.getAttribute("line"));
                    break;

                case "data":
                    cleanupExpr(childElement(ce, "values"));

            }
        });
    }

    private void neg(Element neg) {
        Node next = neg.getNextSibling();
        if(next==null) {
            throw new IllegalStateException("missing pow rhs");
        }

        // push into value
        if(next.getNodeName().equals("val")) {
            next.insertBefore(createTextNode("-"), next.getFirstChild());
            neg.getParentNode().removeChild(neg);
        } else {
            // encapsulate next expression
            neg.appendChild(next);
        }
    }

    private void pow(Element pow) {
        Node prev = pow.getPreviousSibling();
        Node next = pow.getNextSibling();

        if(prev==null || next==null) {
            throw new IllegalStateException("missing pow rhs");
        }

        pow.appendChild(prev);
        pow.appendChild(createTextNode(","));
        pow.appendChild(next);
    }

    private void prod(Element op) {
        op(op, "prod");
    }

    private void sum(Element op) {
        op(op, "sum");
    }

    private boolean isFill(Node node) {
        if(node==null)
            return false;

        String name = node.getNodeName();
        switch(name) {
            case "f":
            case "#text":
                return true;
        }

        return false;
    }

    private void op(Element op, String name) {
        Node parent = op.getParentNode();

        // operator with surrounding text
        List<Node> ops = new ArrayList<>();

        Node prev = op.getPreviousSibling();

        while(isFill(prev)) {
            ops.add(prev);
            prev = prev.getPreviousSibling();
        }

        ops.add(op);

        Node next = op.getNextSibling();

        while(isFill(next)) {
            ops.add(next);
            next = next.getPreviousSibling();
        }

        if(prev==null || next==null) {
            throw new IllegalStateException("missing mul rhs");
        }

        if(prev.getNodeName().equals(name)) {
            ops.forEach(prev::appendChild);
            prev.appendChild(next);
        } else {
            Element opel = createElement(name);
            parent.insertBefore(opel, prev);

            opel.appendChild(prev);
            ops.forEach(opel::appendChild);
            opel.appendChild(next);
        }
    }

    private void debug() {
    }

    void cleanupExpr(Element expr) {
        childElements(expr, "neg").forEach(this::neg);
        childElements(expr, "pow").forEach(this::pow);
        childElements(expr, "mul", "div").forEach(this::prod);
        childElements(expr, "add", "sub").forEach(this::sum);
    }

    Variable array(Element e, Function<String, Variable> variables) {
        Variable arr = contextVariable(e, variables);

        Analyzer.childElements(e, "arg").stream()
                .flatMap(ce -> childElements(ce).stream())
                .forEach(ce -> {
                    String name = ce.getNodeName();
                    if ("var".equals(name)) {
                        Variable v = readVariable(ce);
                        arr.dim(v);
                        setup(ce, arr);
                    } else if ("val".equals(name)) {
                        Integer n = Integer.decode(ce.getTextContent());
                        arr.dim(new Constant(n));
                    } else if ("range".equals(name)) {
                        arr.dim(Value.UNDEF);
                        arr.prop(Variable.Prop.ALLOCATABLE);
                    }
                });

        return arr;
    }

    private void common(Element e) {
        CommonAnalyzer common = newCommon(e.getAttribute("name"));
        Analyzer.childElements(e).forEach(ce -> {
            switch(ce.getNodeName()) {
                case "var":
                    contextVariable(ce, common.members::get);
                    break;

                case "arr":
                    array(ce, common.members::get);
                    break;
            }
        });
    }

    private Node createTextNode(String text) {
        return be.getOwnerDocument().createTextNode(text);
    }

    CommonAnalyzer newCommon(String name) {
        CommonAnalyzer common = block.commons.get(name);

        // prepare root or be new root
        CommonAnalyzer root = analyzer.commons.get(name);
        if (root != null)
            common.root = root;
        else
            analyzer.commons.put(name, common);

        return common;
    }

    class CodeBlock {

        final Element code;

        final BiConsumer<? super CodeBlock, ? super Element> codeProcessor;

        CodeBlock(Element code, BiConsumer<? super CodeBlock, ? super Element> codeProcessor) {
            this.code = code;
            this.codeProcessor = codeProcessor;
        }

        // status of variables used within this block
        Locals locals = new Locals();

        /**
         * Create nested code block.
         * @param e element to parse
         */
        void codeBlock(Element e, BiConsumer<CodeBlock, Element> processor) {

            CodeBlock block = new CodeBlock(e, processor) {
                @Override
                void saveLocals() {

                    // drop all unchanged entries
                    locals.entries().removeIf(CodeBlock.this.locals::contains);

                    super.saveLocals();
                }
            };

            // propagate all variables
            block.locals.putAll(locals);

            block.process();

            // analyze / elevate local variables
            block.locals.applyTo(locals);
        }

        void codeBlock(Element e) {
            codeBlock(e, CodeBlock::codeLines);
        }

        Variable readVariable(Element e) {
            Variable variable = CodeAnalyzer.this.readVariable(e);
            if (variable.isLocal())
                locals.read(variable.name);

            return variable;
        }
        
        void assVar(Element e) {
            // expression first
            parseExpr(e);

            Variable variable = blockVariable(e);
            variable.isAssigned(true);

            if (variable.isLocal())
                locals.write(variable.name);
        }

        private void doReturn(Element e) {
            Type type = block.getReturnType();
            if(type==Type.NONE)
                return;

            String name = block.name;
            Variable variable = block.variables.find(name);

            if(variable==null) {
                // todo: return 0.0
                variable = block.variables.get(name);
            }

            locals.read(variable.name);
        }

        private void readData(Element e) {
            for (Element var : childElements(e, "var")) {
                Variable variable = blockVariable(var);
                variable.isAssigned(true);

                if (variable.isLocal())
                    locals.write(variable.name);
            }
        }

        CodeBlock process() {

            codeProcessor.accept(this, code);

            // may have changed scope
            locals.getNames().removeIf(name -> !isLocal(name));

            saveLocals();

            return this;
        }

        void saveLocals() {

            if(!locals.isEmpty()) {

                // prepend local variable usage
                Element variables = createElement("locals");
                code.insertBefore(variables, code.getFirstChild());
                newLine(variables);

                locals.forEach((name, stat) -> {
                    Element element = createElement("var");

                    element.setAttribute("name", name);

                    if(!stat.isUnused())
                        element.setAttribute("type", stat.name());

                    variables.appendChild(element);
                    newLine(variables);
                });
            }
        }

        void codeLines(Element e) {
            childElements(e).forEach(this::codeLine);
        }

        void codeLine(Element e) {
            String name = e.getNodeName();
            switch (name) {
                case "assvar":
                    // tag statement functions
                    assVar(e);
                    break;

                case "assarr":
                    assarr(e);
                    break;

                case "call":
                    codeBlock(e, CodeBlock::call);
                    break;

                case "read":
                    readData(e);

                case "if":
                case "do":
                    codeBlock(e);
                    break;

                case "elif":
                case "while":
                case "cond":
                    parseExpr(e);
                    break;

                case "for":
                    // mark loop index assigned
                    Variable index = blockVariable(e);
                    index.isAssigned(true);
                    locals.write(index.name);
                    parseElements(e);
                    break;

                case "then":
                case "else":
                    codeBlock(e);

                case "F":
                    setLine(e.getAttribute("line"));
                    break;

                case "return":
                    doReturn(e);

                default:
                    name.length();
                    break;
            }
        }


        private void call(Element e) {
            String name = e.getAttribute("name");
            Code external = analyzer.block(name);

            if (external == null)
                throw new RuntimeException("missing dependeny: " + name);

            args(external, e);
        }

        private void fun(Element e) {
            String name = e.getAttribute("name");

            if (block.variables.exists(name)) {

                Variable var = block.variables.get(name);
                if (var.isArray()) {
                    e.setAttribute("scope", "array");
                    Node parent = e.getParentNode();
                    if(parent instanceof Element)
                        if(((Element)parent).getAttribute("returned").equals("true"))
                            e.setAttribute("ref", "true");
                    return;
                } else if (var.context == null)
                    var.context(Context.INTRINSIC);

            } else {
                Code external = analyzer.block(name);
                if (external != null) {
                    args(external, e);
                    e.setAttribute("type", external.returnType.name());
                    return;
                }
            }

            // intrinsic function
            block.functions.add(name);
            e.setAttribute("scope", "intrinsic");

        }

        private void args(Code external, Element e) {
            block.blocks.add(external);

            e.setAttribute("scope", block.name);

            List<Element> args = Analyzer.childElements(e, "arg");

            // lookup external argument types
            if (external.arguments.size() != args.size()) {
                String message = String.format("argument mismatch for %s.%s: got: %d expected: %d",
                        block.name, external.name, args.size(), external.arguments.size());
                throw new IllegalArgumentException(message);
            }

            for (int i = 0; i < args.size(); ++i) {
                Variable vex = external.arguments.get(i);

                boolean returned = vex.isAssigned() || vex.isReferenced();

                Element arg = args.get(i);
                Element var = getVariableElement(arg);

                if (var != null) {
                    Variable v = blockVariable(var);

                    if (vex.isAssigned()) {
                        v.isAssigned(true);
                    }

                    if (returned) {
                        v.setReferenced();
                        var.setAttribute("returned", "true");
                    }

                    arg.setAttribute("type", "var");

                    if (v.isLocal())
                        locals.read(v.name);

                } else {
                    arg.setAttribute("type", "expr");
                    if (returned)
                        arg.setAttribute("returned", "true");
                    parseExpr(arg);
                }
            }
        }

        private void parseExpr(Element expr) {
            parseElements(expr);
            cleanupExpr(expr);
        }

        private void parseElements(Element els) {
            for (Element ce : childElements(els)) {
                parseElement(ce);
            }
        }

        private void parseElement(Element e) {
            String name = e.getNodeName();
            switch (name) {

                case "var":
                    readVariable(e);
                    break;

                case "fun":
                    fun(e);

                    for (Element arg : childElements(e, "arg")) {
                        parseExpr(arg);
                    }

                    break;

                case "b":
                default:
                    parseExpr(e);
                    break;
            }
        }

        Variable assarr(Element e) {
            Variable arr = blockVariable(e);

            if (!arr.isArray()) {
                arr.context(Context.FUNCTION);
                e.setAttribute("scope", "function");
            }

            parseElements(childElement(e, "args"));
            parseExpr(childElement(e, "expr"));

            return arr;
        }


        // extract single variable or null if an expression or constant
        Element getVariableElement(Element e) {
            Element var = null;

            NodeList nodes = e.getChildNodes();
            for (int i = 0; i < nodes.getLength(); ++i) {
                Node node = nodes.item(i);
                if (node instanceof Element) {
                    Element ce = (Element) node;
                    String name = ce.getTagName();
                    if (name.equals("var")) {
                        if (var != null) // multiple elements -> expression
                            return null;
                        var = ce;
                    }
                }
            }

            return var;
        }
    }

    /**
     * Top level code block.
     * @param code to process
     */
    private void code(Element code) {

        new CodeBlock(code, CodeBlock::codeLines) {

            Element functions = null;

            Element functions() {
                Element functions = this.functions;
                if(functions==null) {
                    this.functions = functions = createElement("functions");
                    code.getParentNode().insertBefore(functions, code);
                }

                return functions;
            }

            // potentially local functions
            Variable assarr(Element e) {
                final String name = e.getAttribute("name");

                Variable v = super.assarr(e);

                if(v.context == Context.FUNCTION) {
                    Element functions = functions();

                    Element function = createElement("function");
                    function.setAttribute("name", name);

                    newLine(functions);
                    functions.appendChild(function);

                    List<Node> block = new LinkedList<>();

                    for (Node node = e; node != null; node = node.getPreviousSibling()) {

                        // stop at first break line
                        if ("C".equals(node.getNodeName()) && node.getFirstChild() == null)
                            break;

                        block.add(0, node);
                    }

                    block.forEach(function::appendChild);

                    newLine(function);
                }

                return v;
            }

            CodeBlock process() {
                super.process();

                if(functions!=null)
                    newLine(functions);

                return this;
            }
        }.process();
    }

    public static void main(String ... args) {
        Document document = Analyzer.parse(args);
        Analyzer.analyze(document);
        XmlWriter.writeDocument(document, new File("parsed.xml"));
    }
}
