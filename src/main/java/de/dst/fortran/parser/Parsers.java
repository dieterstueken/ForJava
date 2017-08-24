package de.dst.fortran.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.04.17
 * Time: 11:00
 */
public class Parsers implements Parser {

    final List<Parser> parsers;

    public Parsers() {
        this.parsers = Collections.emptyList();
    }

    public Parsers(List<Parser> parsers) {
        this.parsers = parsers;
    }

    public Parsers(Parser p1, Parser p2) {
        parsers = new ArrayList<>(2);
        parsers.add(p1);
        parsers.add(p2);
    }

    @Override
    public String parse(String line) {
        for (Parser parser : parsers) {
            if(line==null || line.isEmpty())
                return null;
            line = parser.parse(line);
        }

        return line;
    }

    public Parser or(Parser other) {
        List<Parser> xparsers = new ArrayList<>(parsers.size()+1);
        xparsers.addAll(parsers);
        xparsers.add(other);
        return new Parsers(xparsers);
    }
}
