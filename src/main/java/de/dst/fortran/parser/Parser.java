package de.dst.fortran.parser;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 08.02.13
 * Time: 18:49
 */
abstract public class Parser implements AutoCloseable {

    protected final Writer out;

    protected Parser(Writer out) {
        this.out = out;
    }

    abstract public boolean parse(String line);

    public void close() {}

    Parser parser(Predicate<String> parse) {
        return new Parser(out) {
            @Override
            public boolean parse(String line) {
                return parse.test(line);
            }
        };
    }

    Parser parser(Pattern pattern, Predicate<Matcher> match) {
        return new Parser(out) {
            @Override
            public boolean parse(String line) {
                Matcher m = pattern.matcher(line);
                return m.matches() && match.test(m);
            }

            public String toString() {
                return pattern.toString();
            }
        };
    }

    Parser or(Parser other) {
        return new Parsers(this, other);
    }
}
