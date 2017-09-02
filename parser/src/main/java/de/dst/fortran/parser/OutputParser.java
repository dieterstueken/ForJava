package de.dst.fortran.parser;

import org.intellij.lang.annotations.Language;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  24.08.2017 18:39
 * modified by: $Author$
 * modified on: $Date$
 */
abstract public class OutputParser implements Parser, AutoCloseable {

    protected final Writer out;

    protected OutputParser(Writer out) {
        this.out = out;
    }

    public void close() {}

    static Pattern compile(@Language("regexp") String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    OutputParser parser(Pattern pattern, Function<Matcher, String> match) {
        return new OutputParser(out) {
            @Override
            public String parse(String line) {

                if(Parser.isEmpty(line))
                    return line;

                Matcher m = pattern.matcher(line);
                if(m.matches())
                    return match.apply(m);
                else
                    return line;
            }

            public String toString() {
                return pattern.toString();
            }
        };
    }

}
