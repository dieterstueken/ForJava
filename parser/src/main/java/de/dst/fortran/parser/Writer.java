package de.dst.fortran.parser;


import org.xml.sax.XMLReader;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.02.17
 * Time: 14:42
 */
public class Writer implements AutoCloseable {

    public static Writer _open(OutputStream stream) {
        try {
            SAXTransformerFactory stf = ((SAXTransformerFactory) TransformerFactory.newInstance());
            TransformerHandler th = stf.newTransformerHandler();

            Transformer tf = th.getTransformer();
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            th.setResult(new StreamResult(stream));

            return open(new SAXResult(th));
        } catch (TransformerException ex){
            throw new RuntimeException(ex);
        }
    }

    public static Writer open(OutputStream os) {
        try {
            final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter out = xmlOutputFactory.createXMLStreamWriter(os);
            return new Writer(out);
        } catch (XMLStreamException ex){
            throw new RuntimeException(ex);
        }
    }

    public static Writer open(XMLReader target) {
        SAXResult result = new SAXResult(target.getContentHandler());
        return Writer.open(result);
    }

    public static Writer open(Result result) {
        try {
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            return new Writer(xmlOutputFactory.createXMLStreamWriter(result));
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    final XMLStreamWriter output;

    public Writer(XMLStreamWriter output) {
        this.output = output;
    }

    public Writer empty(String name) {
        try {
            output.writeEmptyElement(name);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public Writer start(String name) {
        try {
            output.writeStartElement(name);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public Writer end() {
        try {
            output.writeEndElement();
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public Writer lattribute(String name, String value) {
        return attribute(name, value==null ? null : value.toLowerCase());
    }

    public Writer attribute(String name, String value) {
        try {
            if(value!=null)
                output.writeAttribute(name, value);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public Writer ltext(String name, String text) {
        return text(name, text==null ? null : text.toLowerCase());
    }

    public Writer text(String name, String text) {
        start(name);
        text(text);
        end();
        return this;
    }

    public Writer comment(String comment) {
        try {
            output.writeComment(comment);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public Writer text(String text) {
        try {
            output.writeCharacters(text);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public Writer nl()  {
        try {
            output.writeCharacters("\n");
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            output.close();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }
}
