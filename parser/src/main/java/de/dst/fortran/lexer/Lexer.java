package de.dst.fortran.lexer;

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

    Writer label() {
        if(label!=null) {
            out.text("label", label);
            label = null;
        }
        return out;
    }

    public Lexer(Writer out) {
        this.out = out;
    }


    void space(Token token) {
        switch(token.item) {
            case SPACE:
                out.text(token.get(0));
            default:
                comment(token);
        }
    }

    void comment(Token token) {
        switch(token.item) {
            case LABEL:
                label=token.get(0);
                break;
            case LINUM:
                out.text("l", token.get(0));
                break;
            case CODELINE:
            case CONTLINE:
                out.nl().comment(token.get(0));
                break;
            case COMMENTLINE:
                out.nl().text("c", token.get(0));
                break;
            case COMMENT:
                out.text("c", token.get(0));
                break;
            default:
                throw new IllegalStateException("unexpected: " + token);
        }
    }

    void process(List<Token> tokens) {

        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch(token.item) {
                case FILE:
                    out.start("file").attribute("name", token.get(0));
                    processFile(tokens);
                    break;
                default:
                    comment(token);
            }
        }
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
                    break;
                case SUBROUTINE:
                    out.start("subroutine")
                            .lattribute("name", token.get(0));
                    processFunction(tokens);
                    break;
                case BLOCKDATA:
                    out.start("blockdata")
                            .lattribute("name", token.get(0));
                    processBlockData(tokens);
                    break;
                case ENDFILE:
                    return;
                default:
                    comment(token);
            }
        }
    }

    private void processBlockData(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch (token.item) {
                case DIM:
                    processDim(token, tokens);
                    break;
                case COMMON:
                    processCommon(token, tokens);
                    break;
                case DATA:
                    processData(tokens);
                case END:
                    return;
                default:
                    comment(token);
            }
        }
    }

    private void processData(List<Token> tokens) {
        out.start("data");
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch (token.item) {
                case NAME:
                    out.text("name", token.get(0));
                    break;
                case SEP:
                    out.empty("s");
                    break;
                case BINOP:
                    if(!"/".equals(token.get(0)))
                        throw new IllegalStateException("unexpected: " + token);
                    processDataValues(tokens);
                    break;
                case END:
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
                case CONST:
                    out.text("value", token.get(0));
                    break;
                case SEP:
                    out.empty("s");
                    break;
                case BINOP:
                    if(!"/".equals(token.get(0)))
                        throw new IllegalStateException("unexpected: " + token);
                    return;
                default:
                    space(token);
            }
        }
    }

    private void processDim(Token token, List<Token> tokens) {
        out.start("dim")
                .lattribute("type", token.get(0))
                .lattribute("size", token.get(1));
        while(!tokens.isEmpty()) {
            token = tokens.remove(0);
            switch (token.item) {
                case ALLOCATABLE:
                    out.empty("alloc");
                    break;
                case NAME:
                    out.start("var").lattribute("name, ", token.get(0)).end();
                    break;
                case APPLY:
                    out.start("arr").lattribute("name, ", token.get(0));
                    processDimArr(tokens);
                    break;
                case SEP:
                    out.empty("s");
                    break;
                case ENDLINE:
                    out.close();
                    return;
                default:
                    space(token);
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
                case CONST:
                    out.text("val", token.get(0));
                    break;
                case NAME:
                    out.text("var", token.get(0));
                    break;
                default:
                    space(token);
            }
        }
    }

    private void processCommon(Token token, List<Token> tokens) {
        out.start("common").lattribute("name", token.get(0));
        
        while(!tokens.isEmpty()) {
            token = tokens.remove(0);
                switch (token.item) {
                    case NAME:
                        out.text("var", token.get(0));
                        break;
                    case SEP:
                        out.empty("s");
                        break;
                    case ENDLINE:
                        out.close();
                        return;
                    default:
                        space(token);
                }
           }
    }

    private void processFunction(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch (token.item) {
                case OPEN:
                    processNames(tokens);
                    break;
                case ENDLINE:
                    processCodeLines(tokens);
                    return;
                default:
                    space(token);
            }
        }
    }

    private void processNames(List<Token> tokens) {
        out.start("args");
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch (token.item) {
                case CLOSE:
                    out.end();
                    return;
                case NAME:
                    out.text("var", token.get(0));
                    break;
                case SEP:
                    out.empty("s");
                    break;
                default:
                    space(token);
            }
        }
    }

    private void processCodeLines(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch (token.item) {
                case END:
                    out.end();
                    return;
                default:
                    processCodeLine(token, tokens);
            }
        }
    }

    private void processCodeLine(Token token, List<Token> tokens) {
        switch (token.item) {
            case DIM:
                processDim(token, tokens);
                break;
            case COMMON:
                processCommon(token, tokens);
                break;

            case IF:
                label().start("if").start("cond");
                break;
            case THEN:
                label().end().start("then").start("noop");
            case ELSEIF:
                label().end().start("elif").start("cond");
            case ELSE:
                label().end().start("else");
            case ENDIF:
                label().end().end();

            case DO:
                label().start("do").start("for");
                break;
            case DOWHILE:
                label().start("do").start("while").start("b");
                break;
            case ENDDO:
                label().end();

            case CONTINUE:
                label().empty("continue");
                break;
            default:
                processStmt(token, tokens);
        }
    }

    private void processStmt(Token token, List<Token> tokens) {

        switch (token.item) {
            case GOTO:
                out.text("goto", token.get(0));
                break;
            case CALL:
                label().start("call").lattribute("name", token.get(0));
                processExpr(tokens);
                break;
            case ALLOCATE:
                label().start("allocate");
                processAllocate(tokens);
                break;
            case DEALLOCATE:
                IoArgs("deallocate", tokens);
                break;
            case FIO:
                IoArgs(token.get(0).toLowerCase(), tokens);
                break;
            case FPRINT:
                out.start("print");
                processIO(tokens);
                break;
            case NAME:
                label().start("assign").text("var", token.get(0));
                processExpr(tokens);
                break;
            case APPLY:
                label().start("assign").start("arr").lattribute("name", token.get(0));
                processExpr(tokens);
                break;
            default:
                comment(token);
        }

    }

    private void processExpr(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch (token.item) {
                case ENDLINE:
                    out.end();
                    return;
                default:
                    processExpr(token);
            }
        }
    }

    private void processExpr(Token token) {

        switch (token.item) {
            case TEXT:
                out.text("string", token.get(0));
                break;
            case CONST:
                out.text("val", token.get(0));
                break;
            case NAME:
                out.text("var", token.get(0));
                break;
            case APPLY:
                out.start("fun").lattribute("name", token.get(0));
                break;
            case OPEN:
                out.start("b");
                break;
            case CLOSE:
                out.end();
                break;
            case BINOP:
                out.empty(binop(token.get(0)));
                break;
            case LOGICAL:
                out.empty(token.get(0));
                break;
            case SEP:
                out.empty("s");
                break;
            case ASSIGN:
                out.empty("assign");
                break;
            default:
                comment(token);
        }
    }

    private String binop(String token) {

        switch(token) {
            case "+": return "add";
            case "-": return "sub";
            case "*": return "mul";
            case "/": return "div";
            case "**": return "pow";
        }

        throw new IllegalStateException("unexpected: " + token);
    }

    private void processIO(Token token) {
        switch (token.item) {
            case FMTOPEN:
                out.start("fmt");
                break;
            case FMTCLOSE:
                out.close();
                break;
            case FSTAT:
                out.empty(token.get(0).toLowerCase());
                break;

            case FMT:
                out.ltext("format", token.get(0));
                break;

            case FMTREP:
                out.start("frep").lattribute("rep", token.get(0));
                break;

            default:
                processExpr(token);
        }
    }

    private void processIO(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch (token.item) {
                case ENDLINE:
                    out.end();
                    return;
                default:
                    processIO(token);
            }
        }
    }

    private void IoArgs(String name, List<Token> tokens) {
        out.start(name).start("b");
        processIO(tokens);
    }

    private void processAllocate(List<Token> tokens) {
        out.start("alloc").start("b");
        processIO(tokens);
    }
}
