package de.dst.fortran.lexer.lines;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.10.17
 * Time: 11:47
 */
public class Line {

    final String line;

    public Line(String line) {
        this.line = line;
    }

    public String toString() {
        return line;
    }

    static final List<Function<String, ? extends Line>> LINES = Arrays.asList(
            EmptyLine::match,
            StartLine::match,
            ContLine::match,
            CommentLine::match
    );

    public static Line matchLine(String line) {
        for (Function<String, ? extends Line> matcher : LINES) {
            Line result = matcher.apply(line);
            if(result!=null)
                return result;
        }

        throw new RuntimeException("unexpected line: " + line);
    }

}
