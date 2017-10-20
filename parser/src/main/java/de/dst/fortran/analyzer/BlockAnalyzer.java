package de.dst.fortran.analyzer;

import de.dst.fortran.code.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;
import java.util.function.Function;

import static de.dst.fortran.analyzer.Analyzer.childElements;
import static de.dst.fortran.analyzer.Analyzer.parseType;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.04.17
 * Time: 17:21
 */
public class BlockAnalyzer {

    final Analyzer analyzer;

    final Block block;

    final Element be;

    Boolean ready = false;

    @Override
    public String toString() {
        return block.name;
    }

    BlockAnalyzer(Analyzer analyzer, Element be) {
        this.analyzer = analyzer;
        this.be = be;

        block = new Block(be.getAttribute("name"));
        block.type = be.getNodeName();
        block.returnType = parseType(be.getAttribute("type"));
        block.path = Analyzer.getPath(be);
        be.setAttribute("path", block.path);

        System.out.format("  define %s:%s\n", block.path, block.name);

    }

    Block block() {

        if (ready == null)
            throw new IllegalStateException("dependeny loop: " + block.name);

        if (!ready) {
            String indent = analyzer.indent;
            System.out.format("%sparse %s:%s\n", indent, block.path, block.name);
            analyzer.indent += "    ";
            ready = null;

            parse();

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
                    childElements(ce).forEach(this::codeLine);
                    break;
            }
        });

        // finally refresh arguments again
        //childElements(be, "args").findFirst().ifPresent(this::prepareArgs);
    }

    //void prepareArgs(Element args) {
    //    variables(args, define(block.arguments::get));
    //}

    private void args(Element e) {
        Analyzer.childElements(e, "arg").stream()
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
                            variable(de).type(type);
                        } else if ("arr".equals(de.getNodeName())) {
                            // array or matrix definition
                            array(de, block.variables::get).type(type);
                        }
                    });
                    break;
            }
        });
    }

    private void common(Element e) {
        Common common = newCommon(e.getAttribute("name"));
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

    private void codeLine(Element e) {
        String name = e.getNodeName();
        switch(name) {
            case "assvar":
                block.assign(variable(e));
                break;

            case "assarr":
                variable(e);
                break;

            case "call":
                call(e);
                break;

            case "for":
                variable(e);
                break;

            default:
                // if else do while format io
        }

        parseExpr(e);
    }


    private void call(Element e) {
        String name = e.getAttribute("name");
        Block external = analyzer.block(name);

        if(external==null)
            throw new RuntimeException("missing dependeny: " + name);

        args(external, e);
    }

    private void fun(Element e) {
        String name = e.getAttribute("name");

        if (block.variables.exists(name)) {

            Variable var = block.variables.get(name);
            if(!var.dim.isEmpty()) {
                e.setAttribute("scope", "array");
                return;
            }
        } else {
            Block external = analyzer.block(name);
            if(external!=null) {
                args(external, e);
                return;
            }
        }

        // intrinsic function
        block.functions.add(name);
        e.setAttribute("scope", "intrinsic");
    }

    private void args(Block external, Element e) {
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
            Element arg = args.get(i);
            Element var = getVariable(arg);

            if(var!=null) {
                Variable v = variable(var);

                if(external.assigned(vex))
                    block.assign(v);

                if(vex.isReferenced())
                    v.isReferenced(true);

                var.setAttribute("returned", "true");
            }

            if(var!=null)
                arg.setAttribute("type", "var");
            else
                arg.setAttribute("type", "expr");
        }
    }

    private void parseExpr(Element e) {

        String name = e.getNodeName();
        switch(name) {

            case "var":
                variable(e);
                break;

            case "fun":
                fun(e);
                break;

            case "pow":
                pow(e);
        }

        // parse recursively
        Analyzer.childElements(e).forEach(this::parseExpr);
    }

    void pow(Element pow) {
        Node prev = pow.getPreviousSibling();
        Node next = pow.getNextSibling();
        if(prev!=null && next!=null) {
            pow.appendChild(prev);
            pow.appendChild(createTextNode(","));
            pow.appendChild(next);
        } else {
            pow.setAttribute("error", "true");
        }
    }

    private Node createTextNode(String text) {
        return be.getOwnerDocument().createTextNode(text);
    }

    Common newCommon(String name) {
        Common common = block.commons.get(name);

        // prepare root or be new root
        Common root = analyzer.commons.get(name);
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

        if(context instanceof Common) {
            e.setAttribute("common", context.getName());
        }

        if(context==block)
            if(block.assigned(v))
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
            }
        });

        return  arr;
    }

    Variable variable(Element e) {
        return variable(e, block.variables::get);
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
