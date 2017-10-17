package de.dst.fortran.lexer;

import de.dst.fortran.XmlWriter;
import de.dst.fortran.lexer.item.Item;
import de.dst.fortran.lexer.item.Token;
import de.dst.fortran.lexer.item.Tokenizer;
import org.w3c.dom.Document;

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

    public static Document parse(String... args) {
        List<Token> tokens = Tokenizer.tokenize(args);
        Document document = XmlWriter.newDocument();
        open(document).process(tokens);
        return document;
    }

    static Lexer open(Document document) {
        return new Lexer(XmlWriter.open(document));
    }

    static Lexer open(File result) throws FileNotFoundException {
        return new Lexer(XmlWriter.open(new FileOutputStream(result)));
    }

    static Lexer open(OutputStream stream) {
        return new Lexer(XmlWriter.open(stream));
    }

    public Lexer(XmlWriter out) {
        this.out = out;
    }

    @Override
    public void close() {
        out.close();
    }

    final XmlWriter out;

    String label = null;
    String linum = null;

    RuntimeException unexpected(Object token) {
        String error = token==null ? "null" : token.toString();
        return new IllegalStateException("unexpected: " + error);
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
                out.comment(String.format("%s: %s", linum, token.get(0))).nl();
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
            throw unexpected(token);
    }

    // consume space until end line
    XmlWriter space(List<Token> tokens) {
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

    Token next(List<Token> tokens) {

        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            if(!isSpace(token))
                return token;
        }

        throw unexpected("EOF");
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
                    out.start("decl");
                    processCodeLines(tokens);
                    out.end().nl();
                    break;

                case PROGRAM:
                    out.start("program");
                    space(tokens).nl();
                    out.start("decl");
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
        out.start("decl").nl();
        processCodeLines(tokens);
    }

    private void procesArgs(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = tokens.remove(0);
            switch (token.item) {
                case OPEN:
                    out.start("args");
                    args(tokens);
                    out.end();
                    space(tokens);
                    return;
                case ENDLINE:
                    return;
                default:
                    space(token);
            }
        }
    }

    boolean code = false;
    private void code() {
        if(!code) {
            code = true;
            out.end().start("code").nl();
        }
    }

    XmlWriter label() {
        code();
        if(label!=null) {
            out.ltext("label", label);
            label = null;
        }
        return out;
    }

    private void processCodeLines(List<Token> tokens) {

        if(code)
            throw unexpected("code");

        while(!tokens.isEmpty()) {

            Token token = next(tokens);

            switch (token.item) {

                case END:
                    out.end().nl();
                    code = false;
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
                        .lattribute("type", token.get(0));
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

            case USE:
                out.text("use", token.get(0));
                break;

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
                    args(tokens);
                    break;
                case OPEN:
                    out.start("arr");
                    braced(tokens);
                    break;
                case COMMA:
                    out.text(",");
                    break;
                case SLASH:
                    out.start("values");
                    processDataValues(tokens);
                    out.end();
                    return;
                default:
                    space(token);
            }
        }
    }

    private void processDataValues(List<Token> tokens) {
        while(!tokens.isEmpty()) {

            if(getValue(tokens, false))
                continue;

            Token token = tokens.remove(0);
            switch (token.item) {
                case COMMA:
                    out.text(",");
                    break;
                case SLASH:
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
                case COMMA:
                    out.text(",");
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
        if(token.item==Item.STAR) {
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
                case COMMA:
                    out.text(",");
                    break;
                case RANGE:
                    out.empty("range");
                    break;
                case BOOLEAN:
                case CONST:
                    out.ltext("val", token.get(0));
                    break;
                case NAME:
                    out.start("var").lattribute("name", token.get(0)).end();
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
                    out.start("var").lattribute("name", token.get(0)).end();
                    break;
                case APPLY:
                    out.start("arr");
                    args(tokens);
                    out.end();
                    break;
                case COMMA:
                    out.text(",");
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

            case FORMAT:
                label().start("format");
                processFmt(tokens);
                out.end();
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
                label().start("do").start("for").lattribute("name", token.get(0));
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
            Token token = next(tokens);

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
                label().ltext("goto", token.get(0));
                processExpr(tokens);
                break;

            case CALL:
                label().start("call").lattribute("name", token.get(1));
                if(token.get(0).endsWith("(")) {
                    args(tokens);
                }
                out.end();

                break;

            case ALLOCATE:
                label().start("allocate").start("arr").lattribute("name", token.get(0));
                braced(tokens);
                processIO(tokens);
                out.end();
                break;

            case DEALLOCATE:
                label().start("deallocate").start("arr").lattribute("name", token.get(0)).end();
                processIO(tokens);
                out.end();
                break;

            case FIO:
                label().start(token.get(0).toLowerCase()).start("io");
                processIO(tokens);  // contains closing brace
                out.end();
                processExpr(tokens);
                out.end();
                break;

            case FPRINT:
                label().start("print"); // missing (
                processExpr(tokens);
                out.end();
                break;

            case RETURN:
                label().empty("return");
                space(tokens);
                break;

            case STOP:
                label().start("stop");
                processExpr(tokens);
                out.end();
                break;

            default:
                processAssignment(token, tokens);
        }
    }

    private void processAssignment(Token token, List<Token> tokens) {

        Token next=null;

        switch (token.item) {

            case NAME:
                // lookahead for "="
                next = next(tokens);

                // pass
            case FSTAT:
                // miss interpretation
                label().start("assvar").lattribute("name", token.get(0));
                break;

            case APPLY:
                label().start("assarr").lattribute("name", token.get(0));
                out.start("args");
                args(tokens);
                out.end();

                next = next(tokens);
                break;

            default:
                space(token);
                return;
        }

        // common part: assigned expression
        if(next!=null && next.item!=Item.ASSIGN)
            throw unexpected(token);

        getValue(tokens);

        out.end();
    }

    private boolean getValue(List<Token> tokens) {
        return getValue(tokens, true);
    }

    // try to extract a single value
    private boolean getValue(List<Token> tokens, boolean binop) {

        int count = 0;
        Token token=null;

        tokens:
        while(!tokens.isEmpty()) {
            token = next(tokens);

            switch (token.item) {
                case TEXT:
                    out.text("string", token.get(0));
                    break;

                case BOOLEAN:
                case CONST:
                    out.ltext("val", token.get(0));
                    break;

                case NAME:
                    out.start("var").lattribute("name", token.get(0)).end();
                    break;

                case APPLY:
                    out.start("fun").lattribute("name", token.get(0));
                    args(tokens);
                    out.end();
                    break;

                case BINOP:
                    // allow leading "-"
                    if("-".equals(token.get(0)))
                        out.empty("neg");
                    else
                        break tokens;

                    ++count;
                    // retry next token
                    continue;

                case OPEN:
                    out.start("b");
                    braced(tokens);
                    break;

                default:
                    break tokens;
            }

            // got value, try get binop

            if(binop && getBinop(tokens))
                ++count; // continue with further values
            else
                return true;
        }

        if(count>0)
            throw unexpected(token);

        // silently push back
        tokens.add(0, token);
        return false;
    }

    private boolean getBinop(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = next(tokens);

            switch (token.item) {
                case BINOP:
                    out.empty(binop(token.get(0)));
                    break;

                case CONCAT:
                    out.empty("cat");
                    break;

                case POW:
                    out.empty("pow");
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

    // process list of comma separated arguments
    private void args(List<Token> tokens) {
        out.start("arg");
        while(!tokens.isEmpty()) {
            Token token = next(tokens);
            switch (token.item) {

                case CLOSE:
                    out.end();
                    return;

                case COMMA:
                    out.end().text(",").start("arg");
                    break;

                default:
                    processExpr(token, tokens);
            }
        }
    }

    // process until end line
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

            case CLOSE:
                out.end();
                break;

            case COMMA:
                out.text(",");
                break;

            case RANGE:
                out.empty("range");
                break;

            case ASSIGN:
                out.empty("to");
                break;

            default:
                // hack: push back
                tokens.add(0, token);

                // extract possible value
                if(!getValue(tokens))
                    throw unexpected(token);
        }
    }

    private String binop(String token) {

        switch(token) {
            case "+": return "add";
            case "-": return "sub";
            case "*": return "mul";
            case "/": return "div";
        }

        throw unexpected(token);
    }

    // process until )
    private void processIO(Token token, List<Token> tokens) {
        switch (token.item) {

            case FMTOPEN:
                out.start("fmt");
                processFmt(tokens);
                out.end();
                break;

            case FSTAT:
                out.start("ios").lattribute("name",token.get(0));
                if(!getValue(tokens))
                    throw unexpected(tokens.get(0));
                out.end();

                break;

            //case APPLY:
            //    out.start("arr").lattribute("name", token.get(0));
            //    braced(tokens);
            //    break;
            //
            default:
                processExpr(token, tokens);
        }
    }

    // process until )
    private void processIO(List<Token> tokens) {
        while(!tokens.isEmpty()) {

            if(getValue(tokens))
                continue;

            Token token = next(tokens);

            switch (token.item) {
                case CLOSE:
                    return;

                case COMMA:
                    out.text(",");
                    break;

                case STAR:
                    out.empty("star");
                    break;

                case FMTOPEN:
                    out.start("fmt");
                    processFmt(tokens);
                    out.end();
                    break;

                case FSTAT:
                    out.start("ios").lattribute("name",token.get(0));
                    if(!getValue(tokens))
                        throw unexpected(token);
                    out.end();
                    break;

                default:
                    throw unexpected(token);
            }
        }
    }

    // process until ) or )'
    private void processFmt(List<Token> tokens) {
        while(!tokens.isEmpty()) {
            Token token = next(tokens);
            switch(token.item) {

                case CLOSE:
                case FMTCLOSE:
                    return;

                case FMT:
                    out.ltext("fmi", token.get(0));
                    break;

                case FMTREP:
                    out.start("frep").lattribute("rep", token.get(0));
                    processFmt(tokens);
                    out.end();
                    break;

                case TEXT:
                    out.text("text", token.get(0));
                    break;

                case COMMA:
                    out.text(",");
                    break;

                case SLASH:
                    out.empty("nl");
                    break;

                default:
                    throw unexpected(token);
            }
        }
    }
}
