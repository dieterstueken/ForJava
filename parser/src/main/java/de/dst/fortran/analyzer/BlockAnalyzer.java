package de.dst.fortran.analyzer;

import de.dst.fortran.code.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.function.Function;
import java.util.stream.Stream;

import static de.dst.fortran.analyzer.Analyzer.childElements;

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
        block.returnType = Type.parse(be.getAttribute("type"));
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

        Function<Element, Variable> variables = define(block.variables::get);

        childElements(be).forEach(ce -> {
            switch(ce.getNodeName()) {

                case "args":
                    variables(ce, define(block.arguments::get));
                    break;

                case "decl":
                    decl(ce);
                    break;

                case "code":
                    childElements(ce).forEach(e->parseVariables(e, variables));
                    break;
            }
        });

        // finally refresh arguments again
        childElements(be, "args").findFirst().ifPresent(this::prepareArgs);
    }

    private void decl(Element e) {

        Function<Element, Variable> variables = define(block.variables::get);

        childElements(e).forEach(ce -> {
            switch(ce.getNodeName()) {

                case "common":
                    Common common = newCommon(ce.getAttribute("name"));
                    variables(ce, define(common.members::get));
                    break;

                case "dim":
                    Type type = Type.parse(ce.getAttribute("type"));
                    childElements(ce).forEach(de -> {
                        // plain value
                        if ("var".equals(de.getNodeName())) {
                            variables.apply(de).type(type);
                        } else if ("arr".equals(de.getNodeName())) {
                            // array or matrix definition
                            Variable var = variable(de).type(type);

                            Function<Element, Variable> index = dimel -> {
                                Variable v = variable(dimel);
                                var.dim(v);
                                return setup(dimel, var);
                            };

                            childElements(de).forEach(dimel -> {
                                String name = dimel.getNodeName();
                                if ("var".equals(name)) {
                                    index.apply(dimel);
                                } else if ("val".equals(name)) {
                                    Integer n = Integer.decode(dimel.getTextContent());
                                    var.dim(new Constant(n));
                                } else if ("range".equals(name)) {
                                    var.dim(Value.UNDEF);
                                }
                            });

                            setup(de, var);
                        }
                    });
                    break;
            }
        });
    }

    private void parseVariables(Element e, Function<Element, Variable> define) {

        String name = e.getNodeName();
        Variable var;
        switch(name) {
            case "assvar":
                var = define.apply(e);
                block.assign(var);
                break;

            case "assarr":
                define.apply(e);
                // parse possible arguments and value
                parseVariables(childElements(e), define);
                break;

            case "args":
                parseVariables(childElements(e), define);
                break;

            case "for":
            case "var":
                define.apply(e);
                break;

            case "call":
                name = e.getAttribute("name");
                if(!isBlock(name))
                    throw new RuntimeException("missing dependeny: " + name);

                parseVariables(childElements(e, "args"), define);
                break;

            case "do":
            case "while":
            case "if":
            case "cond":
            case "then":
            case "elif":
            case "else":
                parseVariables(childElements(e), define);
                break;

            case "fun":
                name = e.getAttribute("name");
                // if it is a defined variable
                if (block.variables.exists(name)) {
                    e.setAttribute("scope", "var");
                } else
                if(!isBlock(name)) {
                    block.functions.add(name);
                }
                // parse variables of function call
                parseVariables(childElements(e), define);
                break;
        }
    }

    void prepareArgs(Element args) {
        variables(args, define(block.arguments::get));
    }

    void insertHeader() {
        
        final Document o = be.getOwnerDocument();
        final Node at = be.getFirstChild();
        final String indent = "\n    ";

        Node nl = o.createTextNode(indent);
        be.insertBefore(nl, at);

        for (String name : block.functions) {
            Element f = o.createElement("fun");
            f.setAttribute("name", name);
            be.insertBefore(f, at);
            nl = o.createTextNode(indent);
            be.insertBefore(nl, at);
        }

        for (Block block : block.blocks) {
            Element f = o.createElement("block");
            f.setAttribute("name", block.name);
            be.insertBefore(f, at);
            nl = o.createTextNode(indent);
            be.insertBefore(nl, at);
        }

        for (Common common : block.commons) {
            Element c = o.createElement("com");
            c.setAttribute("name", common.name);
            be.insertBefore(c, at);
            nl = o.createTextNode(indent);
            be.insertBefore(nl, at);
        }

        for (Variable var : block.variables) {
            if(var.context==null) {
                Element v = o.createElement("lvar");
                v.setAttribute("name", var.name);
                v.setAttribute("type", var.type().id);
                if(!block.assigned(var))
                    v.setAttribute("const", "true");

                for (Value dim : var.dim()) {
                    Element d = o.createElement("d");
                    d.setTextContent(dim.toString());
                    v.appendChild(d);
                }

                be.insertBefore(v, at);

                nl = o.createTextNode(indent);
                be.insertBefore(nl, at);
            }
        }
    }

    boolean isBlock(String name) {
        Block block = analyzer.block(name);
        if(block==null)
            return false;

        block.blocks.add(block);
        return true;
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

    Variable variable(Element e) {
        return block.variables.get(e.getAttribute("name"));
    }

    Function<Element, Variable> define(Function<String, Variable> variables) {
        return e -> setup(e, variables.apply(e.getAttribute("name")));
    }

    private void variables(Element e, Function<Element, Variable> define) {
        childElements(e, "var").forEach(define::apply);
    }

    private void parseVariables(Stream<Element> elements, Function<Element, Variable> define) {
        elements.forEach(ce -> parseVariables(ce, define));
    }

}
