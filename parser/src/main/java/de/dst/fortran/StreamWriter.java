package de.dst.fortran;


import org.w3c.dom.Document;
import org.xml.sax.XMLReader;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.02.17
 * Time: 14:42
 */
public class StreamWriter implements AutoCloseable, XmlWriter {

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

    public static StreamWriter open(Document document) {
        return StreamWriter.open(new DOMResult(document));
    }

    public static StreamWriter open(File file) {
        try {
            return open(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static StreamWriter open(OutputStream os) {
        try {
            final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter out = xmlOutputFactory.createXMLStreamWriter(os);
            return new StreamWriter(out);
        } catch (XMLStreamException ex){
            throw new RuntimeException(ex);
        }
    }

    public static StreamWriter open(Result result) {
        try {
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            final XMLStreamWriter out = xmlOutputFactory.createXMLStreamWriter(result);
            return new StreamWriter(out);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }


    public static StreamWriter open(XMLReader target) {
        SAXResult result = new SAXResult(target.getContentHandler());
        return StreamWriter.open(result);
    }

    final XMLStreamWriter output;

    public StreamWriter(XMLStreamWriter output) {
        this.output = output;
    }

    @Override
    public XmlWriter empty(String name) {
        try {
            output.writeEmptyElement(name);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XmlWriter start(String name) {
        try {
            output.writeStartElement(name);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XmlWriter end() {
        try {
            output.writeEndElement();
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XmlWriter attribute(String name, String value) {
        try {
            if(value!=null)
                output.writeAttribute(name, value);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XmlWriter comment(String comment) {
        try {
            output.writeComment(comment);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XmlWriter text(String text) {
        try {
            output.writeCharacters(text);
            return this;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
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
