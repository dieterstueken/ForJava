package de.dst.fortran.lexer;

import de.dst.fortran.lexer.lines.ContLine;
import de.dst.fortran.lexer.lines.Line;
import de.dst.fortran.lexer.lines.StartLine;
import de.dst.fortran.lexer.token.*;

import java.io.*;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 11:17
 */
public class Lexer {

    public static void main(String... args) {

        Lexer lexer = new Lexer();

        Stream.of(args).map(File::new)
                .flatMap(lexer::lines)
                .forEach(System.out::println);
    }

    public Stream<Token> lines(File file) {
        try {
            Stream<Token> stream = lines(new BufferedReader(new FileReader(file)));
            stream = Stream.concat(Stream.of(new Fstart(file)), stream);
            stream = Stream.concat(stream, Stream.of(EndLine.token, Fend.token));
            return stream;
        } catch (FileNotFoundException error) {
            throw new UncheckedIOException(error);
        }
    }

    public Stream<Token> lines(BufferedReader reader) {
        linum=0;
        return reader.lines()
                .map(Line::matchLine)
                .flatMap(this::lines);
    }

    boolean pending = false;
    int linum=0;

    public Stream<Token> lines(Line line) {
        Stream<Token> tokens = line.stream();
        ++linum;

        if(line instanceof StartLine) {
            tokens = Stream.concat(Stream.of(new Linum(linum)), tokens);
        }

        // prepend endLine
        if(pending && ! (line instanceof ContLine)) {
            tokens = Stream.concat(Stream.of(EndLine.token), tokens);
        }

        pending = line instanceof StartLine;
        
        return tokens;
    }
}
