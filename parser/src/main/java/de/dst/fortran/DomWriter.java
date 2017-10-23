package de.dst.fortran;


import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.02.17
 * Time: 14:42
 */
public class DomWriter implements XmlWriter {

    final Element root;
    Element current;

    public DomWriter(Element root) {
        this.root = root;
        this.current = root;
    }

    public static DomWriter create(String rootName) {
        Document doc = DocumentHelper.createDocument();
        return new DomWriter(doc.addElement(rootName));
    }

    @Override
    public XmlWriter empty(String name) {
        current.addElement(name);
        return this;
    }

    @Override
    public XmlWriter start(String name) {
        current = current.addElement(name);
        return this;

    }

    @Override
    public XmlWriter end() {
        current = current.getParent();
        return this;
    }


    @Override
    public XmlWriter attribute(String name, String value) {

        if(value!=null)
            current.addAttribute(name, value);

        return this;
    }

    @Override
    public XmlWriter comment(String comment) {
        current.addComment(comment);
        return this;
    }

    @Override
    public XmlWriter text(String text) {
        current.addText(text);
        return this;
    }
}
