package de.dst.fortran.lexer.item;

import de.dst.fortran.lexer.LineBuffer;
import de.dst.fortran.lexer.lines.CodeLine;
import de.dst.fortran.lexer.lines.Line;
import de.dst.fortran.lexer.lines.StartLine;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
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
            new Tokenizer(writer::println).tokenizeFiles(args);
        }
    }

    public static List<Token> tokenize(String... args) {
        List<Token> tokens = new LinkedList<>();
        new Tokenizer(tokens::add).tokenizeFiles(args);
        return tokens;
    }

    void tokenizeFiles(String... args) {
        Stream.of(args).map(File::new).forEach(this::tokenize);
    }

    final Consumer<? super Token> tokens;

    static final List<Item> items = Arrays.asList(Item.values());

    boolean pending = false;
    Item current = null;
    int linum=0;

    static final Token ENDFILE = Item.ENDFILE.token();
    static final Token ENDLINE = Item.ENDLINE.token();

    void endline() {
        if(pending) {
            tokens.accept(ENDLINE);
            pending = false;
        }

        current = null;
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
                .map(this::matchLine)
                .forEach(this::tokenize);

        endline();
    }

    Line matchLine(String line) {
        ++linum;
        return Line.matchLine(line);
    }

    Token token(LineBuffer line) {
        for (Item item : items) {

            if(item.tokenizer==null)
                break;  // no further tokenizers

            if(item==Item.FMT || item==Item.FMTREP)
                if(current!=Item.FORMAT)
                    continue;

            if(item==Item.SLASH)
                if(current!=Item.FORMAT && current!=Item.DATA)
                    continue;

            if(item==Item.STAR)
                if(current!=Item.FIO && current!=Item.DIM)
                    continue;

            Token token = item.tokenizer.apply(line);

            if(token!=null) {
                switch(item) {
                    case FIO:
                        current = Item.FIO;
                        break;
                    case FORMAT:
                    case FMTOPEN:
                        current = Item.FORMAT;
                        break;
                    case DIM:
                        current = Item.DIM;
                        break;
                    case DATA:
                        current = Item.DATA;
                        break;
                    case FMTCLOSE:
                        current = null;
                        break;
                }
                return token;
            }
        }

        return null;
    }

    void tokenize(Line line) {

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
