package de.dst.fortran.parser;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.02.17
 * Time: 11:11
 */

public class Lines implements AutoCloseable {

    final Writer out;

    public Lines(Writer out) {
        this.out = out;

        out.start("fortran");
    }

    @Override
    public void close() {
        out.end().close();
    }

    void parseFile(final File file) {

        out.nl();
        out.start("file");
        out.attribute("name", file.getName());

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            LineParser parser = new LineParser(out);
            try {
                reader.lines().forEach(parser::parse);
            } finally {
                parser.close();
            }
        } catch (IOException e) {
            out.text("error", e.getMessage());
        } finally {
            out.nl().end().nl();
        }
    }

    public static void parse(XMLStreamWriter out, String ... args) {

        try (Lines lines = new Lines(new Writer(out))) {
            for (String arg : args) {
                File file = new File(arg);
                lines.parseFile(file);
            }
        }
    }

    public static void parse(Result result, String ... args) {

        try {
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter out = xmlOutputFactory.createXMLStreamWriter(result);
            parse(out, args);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String ... args) throws XMLStreamException, FileNotFoundException {
        //PrintStream os = System.out;

        FileOutputStream os = new FileOutputStream("dump.xml");
        final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter out = xmlOutputFactory.createXMLStreamWriter(os);

        parse(out, args);
    }
}
