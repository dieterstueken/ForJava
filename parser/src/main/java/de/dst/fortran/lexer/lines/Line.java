package de.dst.fortran.lexer.lines;

import de.dst.fortran.lexer.token.Token;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.10.17
 * Time: 11:47
 */
public class Line extends Token {

    final String line;

    public Line(String line) {
        this.line = line;
    }

    public String toString() {
        return line;
    }

    public Stream<Token> stream() {
        return Stream.of(this);
    }

    static final List<Function<String, ? extends Line>> LINES = Arrays.asList(
            CommentLine::match,
            StartLine::match,
            ContLine::match,
            EmptyLine::match
    );

    public static Line matchLine(String line) {
        for (Function<String, ? extends Line> matcher : LINES) {
            Line result = matcher.apply(line);
            if(result!=null)
                return result;
        }

        throw new RuntimeException("unexpected line: " + line);
    }

    public static Stream<String> lines(File file) {
        try {
            return new BufferedReader(new FileReader(file)).lines();
        } catch (FileNotFoundException error) {
            throw new UncheckedIOException(error);
        }
    }

    public static void main(String ... args) {
        Stream.of(args).map(File::new).flatMap(Line::lines).map(Line::matchLine).forEach(System.out::println);
    }
}
