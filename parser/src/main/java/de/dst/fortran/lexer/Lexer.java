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

    boolean isComment(Token token) {
        switch(token.item) {
            case LABEL:
                label=token.get(0);
                break;
            case LINUM:
                linum = token.get(0);
                break;
            case CODELINE:
                if(linum!=null)
                    out.ltext("l", linum);
            case CONTLINE:
                out.comment(token.get(0)).nl();
                break;
            case COMMENTLINE:
                String comment = token.get(0);
                if(comment!=null && !comment.isEmpty())
                    out.text("c", token.get(0));
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

    void comment(Token token) {
        if(!isComment(token))
            throw new IllegalStateException("unexpected: " + token);
    }

    void endline(int close) {
        for(int i=0; i<close; ++i)
            out.end();
        out.nl();
    }

    void endline(List<Token> tokens, int close) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch(token.item) {
                case ENDLINE:
                    endline(close);
                    return;
                default:
                    comment(token);
            }
        }
    }

    void process(List<Token> tokens) {
        out.start("fortran").nl();
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch(token.item) {
                case FILE:
                    out.start("file").attribute("name", token.get(0));
                    processFile(tokens);
                    out.end().nl();
                    break;
                default:
                    comment(token);
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
                    break;
                case SUBROUTINE:
                    out.start("subroutine")
                            .lattribute("name", token.get(0));
                    processFunction(tokens);
                    break;
                case BLOCKDATA:
                    out.start("blockdata")
                            .lattribute("name", token.get(0));
                    endline(tokens, 0);
                    processBlockData(tokens);
                    break;
                case ENDLINE:
                    endline(0);
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
                    break;
                case END:
                    out.empty("end");
                    endline(tokens, 0);
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
                    return;
                default:
                    comment(token);
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
                case SEP:
                    out.empty("s");
                    break;
                case BINOP:
                    if(!"/".equals(token.get(0)))
                        throw new IllegalStateException("unexpected: " + token);
                    endline(tokens,1);
                    return;
                default:
                    comment(token);
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
                    out.start("var").lattribute("name", token.get(0)).end();
                    break;
                case APPLY:
                    out.start("arr").lattribute("name", token.get(0));
                    processDimArr(tokens);
                    break;
                case SEP:
                    out.empty("s");
                    break;
                case ENDLINE:
                    out.end().nl();
                    return;
                default:
                    comment(token);
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
                    comment(token);
            }
        }
    }

    private void processCommon(Token token, List<Token> tokens) {
        out.start("common").lattribute("name", token.get(0));
        
        while(!tokens.isEmpty()) {
            token = tokens.remove(0);
                switch (token.item) {
                    case NAME:
                        out.ltext("var", token.get(0));
                        break;
                    case APPLY:
                        out.start("arr");
                        processBraced(tokens);
                        break;
                    case SEP:
                        out.empty("s");
                        break;
                    case ENDLINE:
                        out.end().nl();
                        return;
                    default:
                        comment(token);
                }
           }
    }

    private void processFunction(List<Token> tokens) {
        procesArgs(tokens);
        processCodeLines(tokens);
    }

    private void procesArgs(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch (token.item) {
                case OPEN:
                    processBraced(tokens);
                    endline(tokens, 0);
                    return;
                case ENDLINE:
                    out.end().nl();
                    return;
                default:
                    comment(token);
            }
        }
    }

    private void processCodeLines(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            if(isComment(token))
                continue;
            
            switch (token.item) {
                case END:
                    out.empty("end");
                    endline(tokens, 0);
                    return;
                default:
                    processCodeLine(token, tokens);
            }
        }
    }

    private void processCodeLine(Token token, List<Token> tokens) {

        // eat leading comments
        while(isComment(token))
            if(tokens.isEmpty())
                return;
            else
                token = tokens.remove(0);

        switch (token.item) {
            case DIM:
                processDim(token, tokens);
                break;
            case COMMON:
                processCommon(token, tokens);
                break;
            case DATA:
                processData(tokens);
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
                endline(tokens, 0);
                break;
            case ENDIF:
                label().end();
                endline(tokens, 1);
                break;

            case DO:
                label().start("do").start("for");
                processExpr(tokens, 1);
                break;
            case DOWHILE:
                label().start("do").start("while").start("b");
                processExpr(tokens, 0);
                break;
            case ENDDO:
                label().end();
                endline(tokens, 0);
                break;

            case CONTINUE:
                label().empty("continue");
                endline(tokens, 0);
                break;

            default:
                processStmt(token, tokens, 0);
        }
    }

    private void processIf(List<Token> tokens) {

        processBraced(tokens);

        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);

            if(isComment(token))
                continue;

            switch (token.item) {
                case THEN:
                    out.start("then");
                    endline(tokens, 0);
                    return;
                default:
                    processStmt(token, tokens, 1);
                    return;
            }
        }
    }

    private void processStmt(Token token, List<Token> tokens, int close) {

        // eat leading comments
        while(isComment(token))
            if(tokens.isEmpty())
                return;
            else
                token = tokens.remove(0);

        switch (token.item) {
            case GOTO:
                out.ltext("goto", token.get(0));
                break;
            case CALL:
                label().start("call").lattribute("name", token.get(0));
                procesArgs(tokens);
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
                label().start("assign").ltext("var", token.get(0));
                processExpr(tokens, close+1);
                break;
            case APPLY:
                label().start("assign").start("arr").lattribute("name", token.get(0));
                processExpr(tokens, close+1);
                break;
            case RETURN:
                out.empty("return");
                endline(tokens, close);
                break;
            case ENDLINE:
                endline(close);
                // empty line
                break;
            default:
                // error?
                endline(tokens, close);
        }

    }

    // process until closed brace
    private void processBraced(List<Token> tokens) {
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

    // process until endline
    private void processExpr(List<Token> tokens, int close) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch (token.item) {
                case ENDLINE:
                    endline(close);
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
                processBraced(tokens);
                break;
            case OPEN:
                out.start("b");
                processBraced(tokens);
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
            case ASSIGN:
                out.empty("to");
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

    private void processIO(Token token, List<Token> tokens) {
        switch (token.item) {
            case FMTOPEN:
                out.start("fmt");
                break;
            case FMTCLOSE:
                out.end();
                break;
            case FSTAT:
                out.empty(token.get(0).toLowerCase());
                break;

            case FMT:
                out.ltext("fmi", token.get(0));
                break;

            case FMTREP:
                out.start("frep").lattribute("rep", token.get(0));
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
                    out.end().nl();
                    return;
                default:
                    processIO(token, tokens);
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
