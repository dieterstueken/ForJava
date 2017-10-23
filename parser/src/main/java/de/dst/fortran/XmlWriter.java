package de.dst.fortran;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  23.10.2017 11:19
 * modified by: $Author$
 * modified on: $Date$
 */
public interface XmlWriter {

    XmlWriter empty(String name);

    XmlWriter start(String name);

    XmlWriter end();

    default XmlWriter lattribute(String name, String value) {
        return attribute(name, value==null ? null : value.toLowerCase());
    }

    XmlWriter attribute(String name, String value);


    default XmlWriter ltext(String name, String text) {
        return text(name, text==null ? null : text.toLowerCase());
    }

    default XmlWriter text(String name, String text) {
        if(text!=null) {
            start(name);
            text(text);
            end();
        }
        return this;
    }

    XmlWriter comment(String comment);

    XmlWriter text(String text);

    default XmlWriter nl()  {
        text("\n");
        return this;
    }

    static Document newDocument() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    static Document readDocument(String file) {

        try(InputStream inputStream = new FileInputStream(file)) {
            StreamSource source = new StreamSource(inputStream);
            SAXTransformerFactory f = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            Transformer tr = f.newTransformer();
            DOMResult result = new DOMResult();
            tr.transform(source, result);
            return (Document) result.getNode();
        }  catch (TransformerException |IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void writeDocument(Document document, File file) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}
