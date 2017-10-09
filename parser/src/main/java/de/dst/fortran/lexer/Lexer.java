package de.dst.fortran.lexer;

import de.dst.fortran.lexer.item.Item;
import de.dst.fortran.lexer.item.Token;
import de.dst.fortran.lexer.item.Tokenizer;
import de.dst.fortran.parser.Writer;

import javax.xml.transform.Result;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.10.17
 * Time: 12:39
 */
public class Lexer implements AutoCloseable {

    public static void main(String... args) throws IOException {

        List<Token> tokens = new LinkedList<>();
        Tokenizer tokenizer = new Tokenizer(tokens::add);
        Stream.of(args).map(File::new).forEach(tokenizer::tokenize);

        try(Lexer lexer = open(new File("dump.xml"))) {
            lexer.process(tokens);
        }
    }

    static Lexer open(File result) throws FileNotFoundException {
        return new Lexer(Writer.open(new FileOutputStream(result)));
    }

    static Lexer open(Result result) {
        return new Lexer(Writer.open(result));
    }

    static Lexer open(OutputStream stream) {
        return new Lexer(Writer.open(stream));
    }

    @Override
    public void close() {
        out.close();
    }

    final Writer out;

    String label = null;
    String linum = null;

    Writer label() {
        if(label!=null) {
            out.ltext("label", label);
            label = null;
        }
        return out;
    }

    public Lexer(Writer out) {
        this.out = out;
    }

    boolean isSpace(Token token) {
        switch(token.item) {
            case LABEL:
                label=token.get(0);
                break;
            case LINUM:
                linum = token.get(0);
                break;
            case CODELINE:
                out.ltext("l", linum);
                out.comment(token.get(0)).nl();
                break;
            case CONTLINE:
                out.nl().comment(token.get(0)).nl();
                break;
            case COMMENTLINE:
                // expect previous endl
                String comment = token.get(0);
                if(comment!=null && !comment.isEmpty())
                    out.text("C", token.get(0));
                out.nl();
                break;
            case COMMENT:
                out.text("c", token.get(0));
                break;
            case SPACE:
                out.text(token.get(0));
                break;
            default:
                return false;
        }
        return true;
    }

    void space(Token token) {
        if(!isSpace(token))
            throw new IllegalStateException("unexpected: " + token);
    }

    // consume space until end line
    Writer space(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch(token.item) {
                case ENDLINE:
                    return out;
                default:
                    space(token);
            }
        }
        return out;
    }

    void process(List<Token> tokens) {
        out.start("fortran").nl();
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch(token.item) {
                case FILE:
                    out.start("file").attribute("name", token.get(0)).nl();
                    processFile(tokens);
                    out.end().nl();
                    break;
                default:
                    space(token);
            }
        }
        out.end();
    }

    void processFile(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            
            switch(token.item) {

                case FUNCTION:
                    out.start("function")
                            .lattribute("name", token.get(2));
                    out.lattribute("type", token.get(0));
                    out.lattribute("size", token.get(1));
                    processFunction(tokens);
                    out.end().nl();
                    break;

                case SUBROUTINE:
                    out.start("subroutine")
                            .lattribute("name", token.get(0));
                    processFunction(tokens);
                    out.end().nl();
                    break;

                case BLOCKDATA:
                    out.start("blockdata")
                            .lattribute("name", token.get(0));
                    space(tokens).nl();
                    processCodeLines(tokens);
                    out.end().nl();
                    break;

                case PROGRAM:
                    out.start("program");
                    space(tokens).nl();
                    processCodeLines(tokens);
                    out.end().nl();
                    break;

                case ENDLINE:
                    out.nl();
                    break;

                case ENDFILE:
                    return;

                default:
                    space(token);
            }
        }
    }

    private void processFunction(List<Token> tokens) {
        procesArgs(tokens);
        out.nl();
        processCodeLines(tokens);
    }

    private void procesArgs(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch (token.item) {
                case OPEN:
                    out.start("args");
                    braced(tokens);
                    space(tokens);
                    return;
                case ENDLINE:
                    return;
                default:
                    space(token);
            }
        }
    }

    private void processCodeLines(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);

            // speedup
            if(isSpace(token))
                continue;

            switch (token.item) {
                case END:
                    out.empty("end");
                    space(tokens).nl();
                    return;
                case ENDLINE:
                    out.nl();
                    break;
                default:
                    processBlockData(token, tokens);
                    out.nl();
            }
        }
    }

    private void processBlockData(Token token, List<Token> tokens) {

        switch (token.item) {
            case DIM:
                out.start("dim")
                        .lattribute("type", token.get(0))
                        .lattribute("size", token.get(1));
                processDim(tokens);
                out.end();
                break;
            case COMMON:
                out.start("common").lattribute("name", token.get(0));
                processCommon(token, tokens);
                out.end();
                break;
            case DATA:
                out.start("data");
                processData(tokens);
                out.end();
                break;
            case END:
                out.empty("end");
                space(tokens);
                return;
            default:
                processCodeLine(token, tokens);
        }
    }

    private void processData(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch (token.item) {
                case NAME:
                    out.text("name", token.get(0));
                    break;
                case APPLY:
                    out.start("arr").lattribute("name", token.get(0));
                    braced(tokens);
                    break;
                case OPEN:
                    out.start("arr");
                    braced(tokens);
                    break;
                case SEP:
                    out.empty("s");
                    break;
                case BINOP:
                    if(!"/".equals(token.get(0)))
                        throw new IllegalStateException("unexpected: " + token);
                    processDataValues(tokens);
                    return;
                default:
                    space(token);
            }
        }
    }

    private void processDataValues(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch (token.item) {
                case BOOLEAN:
                case CONST:
                    out.text("value", token.get(0));
                    break;
                case TEXT:
                    out.text("text", token.get(0));
                    break;
                case SEP:
                    out.empty("s");
                    break;
                case BINOP:
                    if(!"/".equals(token.get(0)))
                        throw new IllegalStateException("unexpected: " + token);
                    space(tokens);
                    return;
                default:
                    space(token);
            }
        }
    }

    private void processDim(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch (token.item) {
                case ALLOCATABLE:
                    out.empty("alloc");
                    break;
                case NAME:
                    out.start("var").lattribute("name", token.get(0));
                    processDimChar(tokens);
                    out.end();
                    break;
                case APPLY:
                    out.start("arr").lattribute("name", token.get(0));
                    processDimArr(tokens);
                    break;
                case SEP:
                    out.empty("s");
                    break;
                case ENDLINE:
                    return;
                default:
                    space(token);
            }
        }
    }

    private void processDimChar(List<Token> tokens) {
        Token token = tokens.get(0);
        if(token.item==Item.BINOP && "*".equals(token.get(0))) {
            token = tokens.get(1);
            if(token.item==Item.CONST) {
                out.lattribute("size", token.get(0));
                tokens.remove(0);
                tokens.remove(0);
            } else if(token.item==Item.WILDCARD) {
                out.attribute("size", "*");
                tokens.remove(0);
                tokens.remove(0);
            }
        }
    }

    private void processDimArr(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch (token.item) {
                case CLOSE:
                    out.end();
                    return;
                case SEP:
                    out.empty("s");
                    break;
                case RANGE:
                    out.empty("range");
                    break;
                case BOOLEAN:
                case CONST:
                    out.ltext("val", token.get(0));
                    break;
                case NAME:
                    out.ltext("var", token.get(0));
                    break;
                default:
                    space(token);
            }
        }
    }

    private void processCommon(Token token, List<Token> tokens) {
        while(!tokens.isEmpty()) {
            token = tokens.remove(0);
            switch (token.item) {
                case NAME:
                    out.ltext("var", token.get(0));
                    break;
                case APPLY:
                    out.start("arr");
                    braced(tokens);
                    break;
                case SEP:
                    out.empty("s");
                    break;
                case ENDLINE:
                    return;
                default:
                    space(token);
            }
        }
    }

    private void processCodeLine(Token token, List<Token> tokens) {

        switch (token.item) {
            case USE:
                out.text("use", token.get(0));
                break;
            case FORMAT:
                out.start("format");
                processIO(tokens);
                break;
            case IF:
                label().start("if").start("cond");
                processIf(tokens);
                break;

            case ELSEIF:
                label().end().start("elif");
                processIf(tokens);
                break;
            case ELSE:
                label().end().start("else");
                space(tokens);
                break;
            case ENDIF:
                label().end().end();
                space(tokens);
                break;

            case DO:
                label().start("do").start("for");
                processExpr(tokens);
                out.end();
                break;

            case DOWHILE:
                label().start("do").start("while");
                braced(tokens);
                space(tokens);
                break;

            case ENDDO:
                label().end();
                space(tokens);
                break;

            case CONTINUE:
                label().empty("continue");
                space(tokens);
                break;

            default:
                processStmt(token, tokens);
        }
    }

    private void processIf(List<Token> tokens) {

        braced(tokens);

        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);

            if(isSpace(token))
                continue;

            switch (token.item) {
                case THEN:
                    out.start("then");
                    space(tokens);
                    return;
                default:
                    processStmt(token, tokens);
                    out.end();
                    return;
            }
        }
    }

    private void processStmt(Token token, List<Token> tokens) {

        switch (token.item) {

            case GOTO:
                out.ltext("goto", token.get(0));
                processExpr(tokens);
                break;

            case CALL:
                label().start("call").lattribute("name", token.get(0));
                procesArgs(tokens);
                out.end();
                break;

            case ALLOCATE:
                IoArgs("allocate", tokens);
                break;

            case DEALLOCATE:
                IoArgs("deallocate", tokens);
                break;

            case FIO:
                IoArgs(token.get(0).toLowerCase(), tokens);
                break;

            case FPRINT:
                out.start("print"); // missing (
                processIO(tokens);
                out.end();
                break;

            case NAME:
                label().start("assign").ltext("var", token.get(0));
                processExpr(tokens);
                out.end();
                break;

            case APPLY:
                label().start("assign").start("arr").lattribute("name", token.get(0));
                processExpr(tokens);
                out.end();
                break;

            case FSTAT:
                // miss interpretation
                label().start("assign").ltext("var", token.get(0));
                processExpr(tokens);
                out.end();
                break;

            case RETURN:
                out.empty("return");
                space(tokens);
                break;

            case STOP:
                out.start("stop");
                processExpr(tokens);
                out.end();
                break;

            default:
                space(token);
        }
    }

    // process until closed brace
    private void braced(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch (token.item) {
                case CLOSE:
                    out.end();
                    return;
                default:
                    processExpr(token, tokens);
            }
        }
    }

    // try to extract a single value
    private boolean getValue(List<Token> tokens) {

        int count = 0;

        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);

            if(isSpace(token))
                continue;

            switch (token.item) {
                case TEXT:
                    out.text("string", token.get(0));
                    break;

                case BOOLEAN:
                case CONST:
                    out.ltext("val", token.get(0));
                    break;

                case NAME:
                    out.ltext("var", token.get(0));
                    break;

                case APPLY:
                    out.start("fun").lattribute("name", token.get(0));
                    braced(tokens);
                    break;

                default:
                    if(count>0)
                        throw new IllegalStateException("unexpected: " + token);

                    // silently push back
                    tokens.add(0, token);
                    return false;
            }

            // got value, try get binop

            if(!getBinop(tokens))
                return true;

            // continue with futher values
            ++count;
        }

        return false;
    }

    private boolean getBinop(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);

            if (isSpace(token))
                continue;

            switch (token.item) {
                case BINOP:
                    out.empty(binop(token.get(0)));
                    break;

                case LOGICAL:
                    out.empty(token.get(0).toLowerCase());
                    break;

                default:// silently push back
                    tokens.add(0, token);
                    return false;
            }

            return true;
        }

        return false;
    }

    // process until endline
    private void processExpr(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch (token.item) {
                case ENDLINE:
                    return;
                default:
                    processExpr(token, tokens);
            }
        }
    }

    private void processExpr(Token token, List<Token> tokens) {

        switch (token.item) {
            case TEXT:
                out.text("string", token.get(0));
                break;
            case BOOLEAN:
            case CONST:
                out.ltext("val", token.get(0));
                break;
            case NAME:
                out.ltext("var", token.get(0));
                break;
            case APPLY:
                out.start("fun").lattribute("name", token.get(0));
                braced(tokens);
                break;
            case OPEN:
                out.start("b");
                braced(tokens);
                break;
            case CLOSE:
                out.end();
                break;
            case BINOP:
                out.empty(binop(token.get(0)));
                break;
            case LOGICAL:
                out.empty(token.get(0).toLowerCase());
                break;
            case SEP:
                out.empty("s");
                break;
            case RANGE:
                out.empty("range");
                break;
            case ASSIGN:
                out.empty("to");
                break;
            default:
                space(token);
        }
    }

    private String binop(String token) {

        switch(token) {
            case "+": return "add";
            case "-": return "sub";
            case "*": return "mul";
            case "/": return "div";
            case "//": return "concat";
            case "**": return "pow";
        }

        throw new IllegalStateException("unexpected: " + token);
    }

    private void processIO(Token token, List<Token> tokens) {
        switch (token.item) {
            case FMTOPEN:
                out.start("fmt");
                break;

            case FMTCLOSE:
                out.end();
                break;

            case FSTAT:
                out.start("ios").lattribute("name",token.get(0));
                if(!getValue(tokens))
                    throw new IllegalStateException("unexpected: " + tokens.get(0));
                out.end();

                break;

            case FMT:
                out.ltext("fmi", token.get(0));
                break;

            case FMTREP:
                out.start("frep").lattribute("rep", token.get(0));
                break;

            case APPLY:
                out.start("arr").lattribute("name", token.get(0));
                braced(tokens);
                break;

                default:
                processExpr(token, tokens);
        }
    }

    private void processIO(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch (token.item) {
                case ENDLINE:
                    return;
                default:
                    processIO(token, tokens);
            }
        }
    }

    private void IoArgs(String name, List<Token> tokens) {
        out.start(name).start("io");
        processIO(tokens);  // contains closing brace
        out.end();
    }
}
