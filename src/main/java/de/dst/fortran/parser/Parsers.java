package de.dst.fortran.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.04.17
 * Time: 11:00
 */
public class Parsers extends Parser {

    final List<Parser> parsers = new ArrayList<Parser>();

    protected Parsers(Parser p1, Parser p2) {
        super(p1.out);
        parsers.add(p1);
        parsers.add(p2);
    }

    @Override
    public boolean parse(String line) {
        for (Parser parser : parsers) {
            if(parser.parse(line))
                return true;
        }

        return false;
    }

    public Parser or(Parser other) {
        parsers.add(other);
        return this;
    }
}
