package de.dst.fortran.code;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  11.10.2017 18:22
 * modified by: $Author$
 * modified on: $Date$
 */
public class Analyzer {

    public final Map<String, BlockAnalyzer> analyzers = new TreeMap<>();

    public final Map<String, CommonAnalyzer> commons = new TreeMap<>();

    public Stream<BlockAnalyzer> units() {
        return analyzers.values().stream();
    }

    public Stream<? extends Common> commons() {
        return commons.values().stream();
    }

    String indent = "";

    Block block(String name) {
        BlockAnalyzer analyzer = analyzers.get(name);
        return analyzer==null ? null : analyzer.block();
    }

    BlockAnalyzer newBlock(Element be) {
        BlockAnalyzer analyzer = new BlockAnalyzer(this, be);
        BlockAnalyzer other = analyzers.put(analyzer.block.name, analyzer);
        if(other!=null)
            throw new IllegalArgumentException("duplicate block: " + analyzer.block.name);

        return analyzer;
    }

    public static Analyzer analyze(Document document) {

        Analyzer analyzer = new Analyzer();

        // prepare blocks to analyze
        childElements(document.getDocumentElement(), "file").stream()
                .peek(fe -> System.out.format("file: %s\n", fe.getAttribute("name")))
                .flatMap(ce -> childElements(ce, "function", "subroutine", "blockdata", "program").stream())
                .forEach(analyzer::newBlock);

        // resolve dependencies: generate all pending blocks
        analyzer.units().forEach(BlockAnalyzer::block);

        return analyzer;
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

    static Element getNextElement(Node node, Predicate<Element> filter) {

        while(node!=null) {
            if(node instanceof Element) {
                Element element = (Element) node;
                if(filter.test(element))
                    return element;
            }
            node = node.getNextSibling();
        }

        return null;
    }

    public static List<Element> childElements(Element e, Predicate<Element> filter) {
        if(e==null)
            return Collections.emptyList();

        Element element = getNextElement(e.getFirstChild(), filter);
        if(element==null)
            return Collections.emptyList();

        List<Element> children = null;

        while(element!=null) {

            Element next = getNextElement(element.getNextSibling(), filter);
            if(children==null) {
                if(next==null) {
                    children = Collections.singletonList(element);
                    break;
                }
                children = new ArrayList<>();
            }

            children.add(element);
            element = next;
        }

        return children;
    }

    public static final Predicate<Element> TRUE = o->true;

    public static final Predicate<Element> ofName(String name) {
        return ce->name.equals(ce.getTagName());
    }

    public static final Predicate<Element> ofName(String name1, String name2) {
        return ofName(name1).or(ofName(name2));
    }

    public static final Predicate<Element> ofName(String ... names) {
        Set<String> set = new HashSet<>(Arrays.asList(names));
        return ce->set.contains(ce.getTagName());
    }

    public static List<Element> childElements(Element e) {
        return childElements(e, TRUE);
    }

    public static Element newLine(Element e) {
        Text text = e.getOwnerDocument().createTextNode("\n");
        e.appendChild(text);
        return e;
    }

    public static List<Element> childElements(Element e, String name) {
        return childElements(e, ofName(name));
    }

    public static List<Element> childElements(Element e, String ... names) {
        return childElements(e, ofName(names));
    }

    public static Element childElement(Element e, String name) {
        List<Element> childElements = childElements(e, name);
        return childElements.size()>0 ? childElements.get(0) : null;
    }


    private static final Map<String, Type> TYPES = new HashMap<>();
    {
        TYPES.put("character*1", Type.CH);
        TYPES.put("integer", Type.I2);
        TYPES.put("integer*2", Type.I2);
        TYPES.put("integer*4", Type.I4);
        TYPES.put("logical*4", Type.L4);
        TYPES.put("real", Type.R4);
        TYPES.put("real*4", Type.R4);
        TYPES.put("real*8", Type.R8);
        TYPES.put("complex", Type.CX);
    }

    public static Type parseType(final String token) {
        if(token==null || token.isEmpty())
            return null;

        Type type = TYPES.get(token);
            if(type!=null)
                return type;

        if(token.startsWith("character*")) {
            return Type.STR; // whatever
        }

        throw new IllegalArgumentException(token);
    }

    public static Integer parseInt(String line) {

        if(line==null || line.isEmpty())
            return null;

        char ch = line.charAt(0);
        if(!Character.isDigit(ch))
            return null;

        int label = 0;

        int len = line.length();
        for(int i=0; i<len; ++i) {
            ch = line.charAt(i);
            if(!Character.isDigit(ch))
                break;
            label = 10 * label + Character.getNumericValue(ch);
        }

        return label;
    }
}
