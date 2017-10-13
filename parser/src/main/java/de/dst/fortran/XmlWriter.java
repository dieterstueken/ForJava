package de.dst.fortran;


import org.w3c.dom.Document;
import org.xml.sax.XMLReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.02.17
 * Time: 14:42
 */
public class XmlWriter implements AutoCloseable {

    public static XmlWriter _open(OutputStream stream) {
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

    public static XmlWriter open(OutputStream os) {
        try {
            final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter out = xmlOutputFactory.createXMLStreamWriter(os);
            return new XmlWriter(out);
        } catch (XMLStreamException ex){
            throw new RuntimeException(ex);
        }
    }

    public static XmlWriter open(Document document) {
        return XmlWriter.open(new DOMResult(document));
    }

    public static XmlWriter open(Result result) {
        try {
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            final XMLStreamWriter out = xmlOutputFactory.createXMLStreamWriter(result);
            return new XmlWriter(out);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public static Document newDocument() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Document readDocument(String file) {

        try(InputStream inputStream = new FileInputStream(file)) {
            StreamSource source = new StreamSource(inputStream);
            SAXTransformerFactory f = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            Transformer tr = f.newTransformer();
            DOMResult result = new DOMResult();
            tr.transform(source, result);
            return (Document) result.getNode();
        }  catch (TransformerException|IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeDocument(Document document, File file) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public static XmlWriter open(XMLReader target) {
        SAXResult result = new SAXResult(target.getContentHandler());
        return XmlWriter.open(result);
    }

    final XMLStreamWriter output;

    public XmlWriter(XMLStreamWriter output) {
        this.output = output;
    }

    public XmlWriter empty(String name) {
        try {
            output.writeEmptyElement(name);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public XmlWriter start(String name) {
        try {
            output.writeStartElement(name);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public XmlWriter end(int count) {
        while(count-->0)
            end();
        return this;
    }

    public XmlWriter end() {
        try {
            output.writeEndElement();
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public XmlWriter lattribute(String name, String value) {
        return attribute(name, value==null ? null : value.toLowerCase());
    }

    public XmlWriter attribute(String name, String value) {
        try {
            if(value!=null)
                output.writeAttribute(name, value);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public XmlWriter ltext(String name, String text) {
        return text(name, text==null ? null : text.toLowerCase());
    }

    public XmlWriter text(String name, String text) {
        if(text!=null) {
            start(name);
            text(text);
            end();
        }
        return this;
    }

    public XmlWriter comment(String comment) {
        try {
            output.writeComment(comment);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public XmlWriter text(String text) {
        try {
            output.writeCharacters(text);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public XmlWriter nl()  {
        try {
            output.writeCharacters("\n");
            output.flush();
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            nl();
            output.writeEndDocument();
            output.close();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }
}
