package de.dst.fortran.analyzer;

import de.dst.fortran.XmlWriter;
import de.dst.fortran.code.Block;
import de.dst.fortran.code.Common;
import de.dst.fortran.lexer.Lexer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  11.10.2017 18:22
 * modified by: $Author$
 * modified on: $Date$
 */
public class Analyzer {

    final Map<String, BlockAnalyzer> analyzers = new TreeMap<>();

    final Map<String, Common> commons = new TreeMap<>();

    String indent = "";

    Block block(String name) {
        BlockAnalyzer analyzer = analyzers.get(name);
        return analyzer==null ? null : analyzer.block();
    }

    public static void main(String ... args) throws Exception {

        Document document = Lexer.parse(args);
        //Document document = Analyzer.readDocument("dump.xml");
        new Analyzer().analyze(document);

        XmlWriter.writeDocument(document, new File("parsed.xml"));
    }

    BlockAnalyzer newAnalyzer(Element be) {
        BlockAnalyzer analyzer = new BlockAnalyzer(this, be);
        BlockAnalyzer other = analyzers.put(analyzer.block.name, analyzer);
        if(other!=null)
            throw new IllegalArgumentException("duplicate block: " + analyzer.block.name);

        return analyzer;
    }

    void analyze(Document document) {

        List<Node> nodes = childNodes(document.getDocumentElement());

        childElements(document.getDocumentElement(), "file")
                .peek(fe -> System.out.format("file: %s\n", fe.getAttribute("name")))
                .flatMap(ce -> childElements(ce, "function", "subroutine", "blockdata", "program"))
                .forEach(this::newAnalyzer);

        analyzers.values().forEach(BlockAnalyzer::block);
    }

    static String getPath(Element be) {
        Element parent = (Element) be.getParentNode();
        String name = parent.getAttribute("name");
        File file = new File(name);
        name = file.getName();
        int dot = name.lastIndexOf('.');
        if(dot>0)
            name = name.substring(0, dot);
        return name.toLowerCase();
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

    public static Stream<Element> childElements(Element e, String ... names) {
        Set<String> set = new HashSet<>(Arrays.asList(names));
        return childElements(e).filter(ce -> {
            String localName = ce.getNodeName();
            return set.contains(localName);
        });
    }
}
