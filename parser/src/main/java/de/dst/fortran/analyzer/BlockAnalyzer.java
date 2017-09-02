package de.dst.fortran.analyzer;

import de.dst.fortran.code.*;
import de.dst.fortran.parser.Lines;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.04.17
 * Time: 17:21
 */
public class BlockAnalyzer {

    public final Block block;

    public Block block() {
        return block;
    }

    @Override
    public String toString() {
        return block.name;
    }

    public BlockAnalyzer(Element be) {
        block = new Block(be.getAttribute("name"));
        block.type = be.getAttribute("type");
        block.returnType = Type.parse(be.getAttribute("return"));

        Function<Element, Variable> variables = define(block.variables::get);

        childElements(be).forEach(ce -> {

            switch(ce.getNodeName()) {
                case "args":
                    variables(ce, define(block.arguments::get));
                    break;

                case "common":
                    Common common = block.commons.get(ce.getAttribute("name"));
                    variables(ce, define(common.members::get));
                    break;

                case "dim":
                    Type type = Type.parse(ce.getAttribute("type"));
                    childElements(ce).forEach(de -> {
                        // plain value
                        if ("var".equals(de.getNodeName())) {
                            variables.apply(de).type(type);
                        } else if ("fun".equals(de.getNodeName())) {
                            // array or matrix definition
                            Variable var = variable(de).type(type);

                            Function <Element, Variable> index = dimel -> {
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
                                } else if("to".equals(name)) {
                                    var.dim(Value.UNDEF);
                                }
                            });

                            setup(de, var);
                        }
                    });
                    break;

                case "c":
                    break;

                default:
                    parseVariables(ce, define(block.variables::get));
                    break;
            }
        });

        // finally refresh arguments again
        childElements(be, "args").findFirst().ifPresent(this::prepareArgs);
    }

    void prepareArgs(Element args) {

        variables(args, define(block.arguments::get));
        
        Document o = args.getOwnerDocument();
        Node p = args.getParentNode();
        Node at = args;

        String indent = "\n    ";

        Node nl = o.createTextNode(indent);
        at = p.insertBefore(nl, at);

        for (String name : block.functions) {
            Element f = o.createElement("fun");
            f.setAttribute("name", name);
            at = p.insertBefore(f, at);

            nl = o.createTextNode(indent);
            at=p.insertBefore(nl, at);
        }

        for (Common common : block.commons) {
            Element c = o.createElement("com");
            c.setAttribute("name", common.name);
            at = p.insertBefore(c, at);

            nl = o.createTextNode(indent);
            at=p.insertBefore(nl, at);
        }

        at = args.getNextSibling();

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

                at = p.insertBefore(v, at);

                nl = o.createTextNode(indent);
                at=p.insertBefore(nl, at);
            }
        }
    }

    public static List<Node> childNodes(Element e) {
        NodeList nodes = e.getChildNodes();
        return new AbstractList<Node>() {

            @Override
            public int size() {
                return nodes.getLength();
            }

            @Override
            public Node get(int index) {
                Node child = nodes.item(index);
                return child;
            }
        };
    }

    public static Stream<Element> childElements(Element e) {
        return childNodes(e).stream()
                .filter(Element.class::isInstance)
                .map(Element.class::cast);
    }

    public static Stream<Element> childElements(Element e, String name) {
        return childElements(e).filter(ce -> {
                String localName = ce.getNodeName();
                return name.equals(localName);
        });
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

    private Function<Element, Variable> assign(Function<Element, Variable> define) {
        // mark variable assigned
        return e -> {
            e.setAttribute("assigned", "true");
            Variable v = define.apply(e);
            block.assign(v);
            return v;
        };
    }

    private void parseVariables(Element e, Function<Element, Variable> define) {
        childElements(e).forEach(ce ->{

            switch(ce.getNodeName()) {

                case "lhs":
                    parseVariables(ce, assign(define));
                    break;

                case "var":
                    define.apply(ce);
                    break;

                case "fun":
                    final String name = ce.getAttribute("name");

                    // if it is a defined variable
                    if (block.variables.exists(name)) {
                        ce.setAttribute("scope", "var");
                    } else {
                        block.functions.add(name);
                    }

                    // parse variables of function call
                    parseVariables(ce, define);
                    break;
            }
        });
    }

    public static void main(String ... args) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Lines.parse(new DOMResult(document), args);

        analyze(document);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult("parsed.xml");
        transformer.transform(source, result);
    }

    static void analyze(Document document) throws TransformerException {

        Map<String, Block> blocks = new HashMap<>();

        childElements(document.getDocumentElement(), "file")
                .peek(fe -> System.out.format("file: %s\n", fe.getAttribute("name")))
                .flatMap(ce -> BlockAnalyzer.childElements(ce, "block"))
                .map(BlockAnalyzer::new)
                .peek(System.out::println)
                .map(BlockAnalyzer::block)
                .forEach(b -> blocks.put(b.name, b));


        Map<String, Common> commons = new HashMap<>();

        blocks.values().stream()
                .flatMap(b -> b.commons.stream())
                .forEach(c -> {
                    System.out.format("common: %s\n", c.getName());
                    Common cx = commons.put(c.getName(), c);
                    if(cx!=null && !cx.equals(c)) {
                        cx.equals(c);
                        System.out.format("different common definitions for %s\n", c.getName());
                    }
                });
    }
}
