package de.dst.fortran.code;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.LinkedList;
import java.util.List;
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
    public Element element() {
        return be;
    }

    @Override
    public String getLine() {
        return line;
    }

    @Override
    public String toString() {
        return block.name;
    }

    CodeAnalyzer(Analyzer analyzer, Element be) {
        this.analyzer = analyzer;
        this.be = be;
        block = new Code(be.getAttribute("name"));
        block.type = be.getNodeName();
        block.returnType = parseType(be.getAttribute("type"), Value.Kind.PRIMITIVE);
        block.path = Analyzer.getPath(be);
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

    Variable arg(Element e) {
        return variable(e, block.arguments::get);
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
                            variable(de).decl(type);
                        } else if ("arr".equals(de.getNodeName())) {
                            // array or matrix definition
                            array(de, block.variables::get).decl(type);
                        }
                    });
                    break;

                case "F":
                    line = ce.getAttribute("line");
                    break;
            }
        });
    }

    private void common(Element e) {
        CommonAnalyzer common = newCommon(e.getAttribute("name"));
        Analyzer.childElements(e).forEach(ce -> {
            switch(ce.getNodeName()) {
                case "var":
                    variable(ce, common.members::get);
                    break;

                case "arr":
                    array(ce, common.members::get);
                    break;
            }
        });
    }

    private void code(Element e) {
        List<Element> code = childElements(e);

        Element functions = null;

        for (Element element : code) {
           if(codeLine(element)) {
               if(functions==null) {
                   functions = e.getOwnerDocument().createElement("functions");
                   e.getParentNode().insertBefore(functions, e);
               }

               Element function = e.getOwnerDocument().createElement("function");
               function.setAttribute("name", element.getAttribute("name"));

               newLine(functions);
               functions.appendChild(function);

               List<Node> block = new LinkedList<>();

               for(Node node=element; node!=null; node = node.getPreviousSibling()) {

                   // stop at first break line
                   if("C".equals(node.getNodeName()) && node.getFirstChild()==null)
                       break;

                   block.add(0, node);
               }

               block.forEach(function::appendChild);

               newLine(function);
               //newLine(functions);
           }
        }

        if(functions!=null)
            newLine(functions);
    }

    private void codeLines(Element e) {
        childElements(e).forEach(this::codeLine);
    }

    private boolean codeLine(Element e) {
        String name = e.getNodeName();
        switch(name) {
            case "assvar":
                // tag statement functions
                variable(e).isAssigned(true);
                parseExpr(childElements(e));
                break;

            case "assarr":
                return assarr(e).context==Context.FUNCTION;

            case "call":
                call(e);
                break;

            case "if":
            case "do":
                codeLines(e);
                break;

            case "then":
            case "else":
                codeLines(e);
                break;

            case "for":
                variable(e);
                break;

            case "cond":
            case "elif":
            case "while":
                parseExpr(childElements(e));
                break;

            case "F":
                line = e.getAttribute("line");
                break;

            default:
                name.length();
                break;
        }

        return false;
    }

    private void call(Element e) {
        String name = e.getAttribute("name");
        Code external = analyzer.block(name);

        if(external==null)
            throw new RuntimeException("missing dependeny: " + name);

        args(external, e);
    }

    private void fun(Element e) {
        String name = e.getAttribute("name");

        if (block.variables.exists(name)) {

            Variable var = block.variables.get(name);
            if(var.isArray()) {
                e.setAttribute("scope", "array");
                return;
            } else
            if(var.context==null)
                var.context(Context.INTRINSIC);

        } else {
            Code external = analyzer.block(name);
            if(external!=null) {
                args(external, e);
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

        if(external.arguments.size()!=args.size()) {
            String message = String.format("argument mismatch for %s.%s: got: %d expected: %d",
                    block.name, external.name, args.size(), external.arguments.size());
            throw new IllegalArgumentException(message);
        }

        for(int i=0; i<args.size(); ++i) {
            Variable vex = external.arguments.get(i);

            boolean returned = vex.isAssigned() || vex.isReferenced();

            Element arg = args.get(i);
            Element var = getVariable(arg);

            if(var!=null) {
                Variable v = variable(var);

                if(vex.isAssigned()) {
                    v.isAssigned(true);
                }

                if(returned) {
                    v.setReferenced();
                    var.setAttribute("returned", "true");
                }

                arg.setAttribute("type", "var");
            } else {
                arg.setAttribute("type", "expr");
                if(returned)
                    arg.setAttribute("returned", "true");
                parseExpr(childElements(arg));
            }
        }
    }

    private void parseExpr(List<Element> expr) {

        for (Element e : expr) {
            String name = e.getNodeName();
            switch (name) {

                case "var":
                    variable(e);
                    break;

                case "fun":
                    fun(e);
                    parseExpr(childElements(e));
                    break;

                case "b":
                default:
                    parseExpr(childElements(e));
                    break;
            }
        }
    }

    void pow(Element pow) {
        Node prev = pow.getPreviousSibling();
        Node next = pow.getNextSibling();
        if(prev!=null && next!=null) {
            pow.appendChild(prev);
            pow.appendChild(createTextNode(","));
            pow.appendChild(next);
        } else {
            throw new IllegalStateException("missing pow rhs");
        }
    }

    private Node createTextNode(String text) {
        return be.getOwnerDocument().createTextNode(text);
    }

    CommonAnalyzer newCommon(String name) {
        CommonAnalyzer common = block.commons.get(name);

        // prepare root or be new root
        CommonAnalyzer root = analyzer.commons.get(name);
        if(root!=null)
            common.root = root;
        else
            analyzer.commons.put(name, common);

        return common;
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

        if(context instanceof CommonAnalyzer) {
            e.setAttribute("common", context.getName());
        }

        if(context==block)
            if(v.isAssigned())
                e.setAttribute("assigned", "true");

        if(v.type!=null) {
            e.setAttribute("type", v.type.toString());
        }

        return v;
    }

    Variable variable(Element e, Function<String, Variable> variables) {
        final String name = e.getAttribute("name");
        if(name==null || name.isEmpty())
            throw new IllegalArgumentException("no name");

        final Variable var = variables.apply(name);
        return setup(e, var);
    }

    Variable array(Element e, Function<String, Variable> variables) {
        Variable arr = variable(e, variables);

        Analyzer.childElements(e, "arg").stream()
                .flatMap(ce -> childElements(ce).stream())
                .forEach(ce -> {
            String name = ce.getNodeName();
            if ("var".equals(name)) {
                Variable v = variable(ce);
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

        return  arr;
    }

    Variable assarr(Element e) {
        Variable arr = variable(e);

        if(!arr.isArray()) {
            arr.context(Context.FUNCTION);
            e.setAttribute("scope", "function");
        } else {
            // parse arguments and rhs
            parseExpr(childElements(childElement(e, "expr")));
        }

        return arr;
    }

    Variable variable(Element e) {
        if(e!=null)
            return variable(e, block.variables::get);
        return null;
    }

    // extract single variable or null if an expression or constant
    Element getVariable(Element e) {
        Element var = null;

        NodeList nodes = e.getChildNodes();
        for(int i=0; i<nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            if(node instanceof Element) {
                Element ce = (Element) node;
                String name = ce.getTagName();
                if(name.equals("var")) {
                    if(var!=null) // multiple elements -> expression
                        return null;
                    var = ce;
                }
            }
        }

        return var;
    }
}
