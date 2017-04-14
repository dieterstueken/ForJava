package de.dst.fortran.parser;

import de.dst.fortran.code.Block;
import de.dst.fortran.code.Common;
import de.dst.fortran.code.Variable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.dom.DOMResult;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.04.17
 * Time: 13:05
 */
public class Analyzer {

    final Document document;

    List<Block> blocks = new ArrayList<>();

    public Analyzer(Document document) {
        this.document = document;
    }

    public void parse(String ... args) throws XMLStreamException {
        Lines.parse(new DOMResult(document), args);
    }

    public static Analyzer create() {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            //factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            return new Analyzer(document);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String ... args) throws XMLStreamException, FileNotFoundException, ParserConfigurationException {

        Analyzer analyzer = create();
        analyzer.parse(args);

        analyzer.analyze();
    }

    public static List<Node> childNodes(Element e) {
        NodeList nodes = e.getChildNodes();
        return new AbstractList<Node> () {

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

    private void analyzeBlock(Element be) {
        BlockAnalyzer analyzer = new BlockAnalyzer(be);

        analyzer.dump();

        Block block = analyzer.block;
        blocks.add(block);
    }

    private void analyze() {
        childElements(document.getDocumentElement(), "file")
                .flatMap(ce -> childElements(ce, "block"))
                .forEach(this::analyzeBlock);

        Map<String, Common> commons = new HashMap<>();

        for (Block block : blocks) {
            for (Common common : block.commons.values()) {
                Common other = commons.get(common.name);
                if(other!=null) {
                    compare(other, other);
                } else
                    commons.put(common.name, common);
            }

        }
    }

    private static void compare(Common c1, Common c2) {
        if(c1.members.size()!=c2.members.size()) {
            System.out.format("different size of %s: %d %d\n", c1.name, c1.members.size(), c2.members.size());

            for (Variable v1 : c1.members.values()) {
                Variable v2 = c2.members.entities.get(v1.name);
                if(v2==null)
                    System.out.format("missing %s.%s\n", c1.name, v1.name);

                if(!Objects.equals(v1.type, v2.type)) {
                    System.out.format("different types: %s.%s\n", c1.name, v1.name);
                }

                if(!Objects.equals(v1.dim(), v2.dim())) {
                    System.out.format("different dimensions: %s.%s\n", c1.name, v1.name);
                }
            }
        }
    }

}
