package de.dst.fortran.code;

import de.dst.fortran.StreamWriter;
import de.dst.fortran.XmlWriter;
import de.dst.fortran.lexer.Lexer;
import de.dst.fortran.lexer.item.Token;
import de.dst.fortran.lexer.item.Tokenizer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  11.10.2017 18:22
 * modified by: $Author$
 * modified on: $Date$
 */
public class Analyzer {

    public final Map<String, CodeAnalyzer> analyzers = new TreeMap<>();

    public final Map<String, CommonAnalyzer> commons = new TreeMap<>();

    public Collection<CodeAnalyzer> units() {
        return analyzers.values();
    }

    public Collection<? extends Common> commons() {
        return commons.values();
    }

    String indent = "";

    Code block(String name) {
        CodeAnalyzer analyzer = analyzers.get(name);
        return analyzer==null ? null : analyzer.code();
    }

    CodeAnalyzer newBlock(Element be) {
        CodeAnalyzer analyzer = new CodeAnalyzer(this, be);
        CodeAnalyzer other = analyzers.put(analyzer.block.name, analyzer);
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
        analyzer.units().forEach(CodeAnalyzer::code);

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

    public static Type parseType(final String token) {
        return Type.parse(token);
    }

    public static TypeDef parseType(final String token, Value.Kind kind) {
        Type type = Type.parse(token);
        return type==null ? null : type.kind(kind);
    }

    public static Document parse(String... args) {
        List<Token> tokens = Tokenizer.tokenize(args);
        Document document = XmlWriter.newDocument();
        new Lexer(StreamWriter.open(document)).process(tokens);
        return document;
    }

}
