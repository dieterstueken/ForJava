package de.dst.fortran.lexer.item;

import de.dst.fortran.lexer.LineBuffer;
import de.dst.fortran.lexer.lines.CodeLine;
import de.dst.fortran.lexer.lines.Line;
import de.dst.fortran.lexer.lines.StartLine;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 06.10.17
 * Time: 14:15
 */
public class Tokenizer {

    public static void main(String... args) throws IOException {

        try(PrintWriter writer = new PrintWriter("items.txt")) {

            Tokenizer tokenizer = new Tokenizer(writer::println);

            Stream.of(args).map(File::new).forEach(tokenizer::tokenize);
        }
    }

    final Consumer<? super Token> tokens;

    static final List<Item> items = Arrays.asList(Item.values());

    boolean pending = false;
    int linum=0;

    static final Token ENDFILE = Item.ENDFILE.token();
    static final Token ENDLINE = Item.ENDLINE.token();

    void endline() {
        if(pending) {
            tokens.accept(ENDLINE);
            pending = false;
        }
    }

    public Tokenizer(Consumer<? super Token> tokens) {
        this.tokens = tokens;
    }

    public void tokenize(File file) {
        try {
            tokens.accept(Item.FILE.token(file.getName()));
            tokenize(new BufferedReader(new FileReader(file)));
            tokens.accept(ENDFILE);
        } catch (FileNotFoundException error) {
            throw new UncheckedIOException(error);
        }
    }

    public void tokenize(BufferedReader reader) {
        linum=0;
        reader.lines()
                .map(Line::matchLine)
                .forEach(this::tokenize);

        endline();
    }

    Token token(LineBuffer line) {
        for (Item item : items) {
            if(item.tokenizer!=null) {
                Token token = item.tokenizer.apply(line);
                if(token!=null)
                    return token;
            }
        }

        return null;
    }

    void tokenize(Line line) {
        ++linum;

        if(line instanceof CodeLine) {
            tokenize((CodeLine) line);
        } else {
            endline();
            tokens.accept(Item.COMMENTLINE.token(line.toString()));
        }
    }

    void startLine(StartLine line) {
        endline();
        tokens.accept(Item.LINUM.token(Integer.toString(linum)));
        tokens.accept(Item.CODELINE.token(line.toString()));

        pending = true;

        String label = line.label();
        if(label!=null)
            tokens.accept(Item.LABEL.token(label));
    }

    void tokenize(CodeLine code) {

        if(code instanceof StartLine) {
            startLine((StartLine) code);
        } else
            tokens.accept(Item.CONTLINE.token(code.toString()));

        LineBuffer line = new LineBuffer(code.code);

        while (!line.isEmpty()) {
            Token token = token(line);
            if (token != null) {
                tokens.accept(token);
            } else {
                token = token(line);
                throw new RuntimeException(line.toString());
            }
        }
    }
}
