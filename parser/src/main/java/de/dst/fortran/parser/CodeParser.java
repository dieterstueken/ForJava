package de.dst.fortran.parser;

import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.04.17
 * Time: 15:27
 */
public class CodeParser extends OutputParser {

    static final Pattern SUBROUTINE = compile("subroutine\\s*(\\w+)\\s*(\\()?(.*)");
    static final Pattern FUNCTION = compile("(\\w+\\S*)?\\s*function\\s*(\\w+)\\s*\\((.*)");
    static final Pattern BLOCKDATA = compile("block\\s+data\\s+(\\w+)\\s*");
    static final Pattern DATA = compile("data\\s+(\\w+)\\s*");
    static final Pattern DIM = compile("((integer|real|character)(\\*\\d+)?)(.*)");
    static final Pattern ALLOCATABLE = compile("allocatable\\s*::\\s(.*)");
    static final Pattern ALLOCATE = compile("allocate\\s*\\((.*)");
    static final Pattern DEALLOCATE = compile("deallocate\\s*\\((.*)");
    static final Pattern COMMON = compile("common\\s*/(\\w+)/\\s*(.*)");
    static final Pattern ASSIGN = compile("(\\w+[^⁼]*)=(.*)");
    static final Pattern BOP = compile(":(eq|ne|le|lt|ge|gt|and|or):(.*)");
    static final Pattern IF = compile("if\\s*\\((.*)");
    static final Pattern THEN = compile("then\\s*");
    static final Pattern ELSE = compile("else\\s*");
    static final Pattern ELSEIF = compile("else\\s+if\\s*\\((.*)");
    static final Pattern ENDIF = compile("end\\s*if\\s*");
    static final Pattern DO = compile("do\\s+(\\w+)\\s*=([^,]*),\\s*([^,]*)\\s*(,.*)?");
    static final Pattern DOWHILE = compile("do\\s+while\\s*\\((.*)");
    static final Pattern ENDDO = compile("end\\s*do\\s*");
    static final Pattern CONTINUE = compile("continue\\s*");
    static final Pattern NAME = compile("(\\w+)(.*)");
    static final Pattern FUN = compile("(\\w+)\\s*\\((.*)");
    static final Pattern CALL = compile("call\\s+(\\w+)\\s*(.*)");
    static final Pattern GOTO = compile("goto\\s+(\\d+)");
    static final Pattern BRCLOSE = compile("\\)(.*)");
    static final Pattern BROPEN = compile("\\((.*)");

    static final Pattern IO = compile("(open|close|read|write) *\\(([^,)]*),(.*)");
    static final Pattern ATTRIBUTE = compile("(\\w+)%(\\w+)(.*)");
    static final Pattern PRINT = compile("print *,\\*(.*)");
    static final Pattern FORMAT = compile("format *\\((.*)");
    // multi line format strings
    static final Pattern FMTBEG = compile("['\"]\\((.*)");
    static final Pattern FMTEND = compile("\\)[\"'](.*)");
    static final Pattern STAR = compile("\\*(.*)");
    static final Pattern FMP = compile("([\\d\\.\\w]+) *(.*)");
    static final Pattern FMREP = compile("(\\d+) *\\( *(.*)");

    static final Pattern CONST = compile("((\\.\\d+)|(\\d+(\\.\\d*)?))(.*)");
    static final Pattern STRING1 = compile("'([^\']*)\'(.*)");
    static final Pattern STRING2 = compile("\"([^\"]*)\"(.*)");
    static final Pattern SEP = compile(",(.*)");
    static final Pattern END = compile("end\\s*");
    static final Pattern RETURN = compile("return\\s*");
    static final Pattern SPACE = compile("(\\s+)(.*)");
    static final Pattern RANGE = compile(":(.*)");

    static String op(char op) {
        switch(op) {
            case '+':
                return "add";
            case '-':
                return "sub";
            case '*':
                return "mul";
            case '/':
                return "div";
        }
        return null;
    }

    Runnable finish = null;

    // a THEN occurred, no shortcut endif
    boolean then=false;

    // any label assigned
    String label = null;

    private void fi() {
        // terminate pending if
        if(!then)
            out.end();
    }

    void finish(Runnable newfinish) {
        if(this.finish==null)
            this.finish = newfinish;
        else {
            // cascade
            Runnable oldfinish = this.finish;
            this.finish = () -> {
                newfinish.run();
                oldfinish.run();
            };
        }
    }

    // start a new code line
    public void nl() {
        Runnable f = this.finish;
        this.finish = null;
        if(f!=null)
            f.run();

        if(label!=null) {
            // notify a missed label
            out.text("xlabel", label);
            label = null;
        }
        out.nl();

        current = main;
    }

    // open an auto closed tag for this line
    Writer enclose(String name) {
        finish(() -> {
            name.length();
            out.end();
        });
        return out.start(name);
    }

    final Parser expr = parser(SPACE, m -> {
        out.text(m.group(1));
        return parseExpr(m.group(2));
    }).or(parser(CALL, m -> {
        enclose("call").lattribute("name", m.group(1));
        return parseExpr(m.group(2));
    })).or(parser(PRINT, m->{
        enclose("print");
        return parseExpr(m.group(1));
    })).or(parser(ALLOCATABLE, m->{
        out.empty("allocatable");
        return parseExpr(m.group(1));
    })).or(parser(IO, m -> {
        enclose(m.group(1).toLowerCase()).lattribute("ch", m.group(2));
        out.start("args");
        return startFmt(m.group(3));
    })).or(parser(ATTRIBUTE, m->{
        out.start("attrib").lattribute(m.group(1).toLowerCase(), m.group(2)).end();
        return parseExpr(m.group(3));
    })).or(parser(ASSIGN, m -> {
        out.start("assign");
        finish(out::end);
        finish(out::end);
        out.start("lhs");
        parseExpr(m.group(1));
        out.end().start("rhs");
        return parseExpr(m.group(2));
    })).or(parser(BROPEN, m -> {
        out.start("b");
        return parseExpr(m.group(1));
    })).or(parser(BRCLOSE, m -> {
        out.end();
        return parseExpr(m.group(1));
    })).or(parser(STRING1, m -> {
        out.text("str", m.group(1));
        return parseExpr(m.group(2));
    })).or(parser(STRING2, m -> {
        out.text("str", m.group(1));
        return parseExpr(m.group(2));
    })).or(parser(BOP, m -> {
        out.empty(m.group(1).toLowerCase());
        return parseExpr(m.group(2));
    })).or(line -> {
        String op = op(line.charAt(0));
        if (op != null) {
            out.empty(op);
            return parseExpr(line.substring(1));
        }
        return line;
    }).or(parser(SEP, m -> {
            out.empty("sep");
            return parseExpr(m.group(1));
    })).or(parser(RANGE, m -> {
        out.empty("to");
        return parseExpr(m.group(1));
    })).or(parser(GOTO, m -> {
        out.text("goto", m.group(1));
        return null;
    })).or(parser(RETURN, m -> {
        out.empty("return");
        return null;
    })).or(parser(THEN, m -> {
        if (!then) {
            out.start("then");
            then = true;
        }
        return null;
    })).or(parser(FUN, m -> {
        String name = m.group(1);
        out.start("fun")
                .lattribute("name", name);
        return parseExpr(m.group(2));
    })).or(parser(CONST, m -> {
        String name = m.group(1);
        out.ltext("val", name);
        return parseExpr(m.group(5));
    })).or(parser(NAME, m -> {
        String name = m.group(1);
        //out.text("var", name);
        out.start("var").lattribute("name", name).end();
        return parseExpr(m.group(2));
    }));

    final Parser main = parser(SPACE, m->{
        out.text(m.group(1));
        // continue with remaining part
        return m.group(2);
    }).or(parser(SUBROUTINE, m -> {
        out.start("block")
                .attribute("type", "subroutine")
                .lattribute("name", m.group(1));
        if(m.group(2)!=null)
            out.start("args");
        return parseExpr(m.group(3));
    })).or(parser(FUNCTION, m -> {
        out.start("block")
                .attribute("type", "function")
                .lattribute("return", m.group(1))
                .lattribute("name", m.group(2));
        out.start("args");
        return parseExpr(m.group(3));
    })).or(parser(BLOCKDATA, m -> {
        out.start("block")
                .attribute("type", "data")
                .lattribute("name", m.group(1));
        return null;
    })).or(parser(DATA, m -> {
        out.start("data")
                .lattribute("name", m.group(1));
        finish(out::end);
        return null;
    })).or(parser(COMMON, m -> {
        out.start("common")
                .lattribute("name", m.group(1));
        finish(out::end);
        return parseExpr(m.group(2));
    })).or(parser(DIM, m -> {
        out.start("dim").lattribute("type", m.group(1));
        finish(out::end);
        return parseExpr(m.group(4));
    })).or(parser(ALLOCATE, m -> {
        out.start("allocate");
        return parseExpr(m.group(1));
    })).or(parser(DEALLOCATE, m -> {
        out.start("deallocate");
        return parseExpr(m.group(1));
    })).or(parser(FORMAT, m -> {
        out.start("fmt");
        if (label != null) {
            out.attribute("label", label);
            label = null;
        }
        return startFmt(m.group(1));
    })).or(parser(END, m -> {
        out.end();
        return null;
    })).or(parser(IF, m -> {
        out.start("if").start("cond");
        then = false;
        finish(this::fi); // close if inline if
        return parseExpr(m.group(1));
    })).or(parser(ELSE, m -> {
        out.end().start("else");
        then = false;
        return null;
    })).or(parser(ELSEIF, m -> {
        out.end().start("elif").start("cond");
        then = true; // don't open a then
        return parseExpr(m.group(1));
    })).or(parser(ENDIF, m -> {
        // (then|else|elif) endif
        out.end().end();
        return null;
    })).or(parser(DO, m -> {
        out.start("do")
                .lattribute("var", m.group(1))
                .start("start");
        parseExpr(m.group(2));
        out.end().start("end");
        parseExpr(m.group(3));
        out.end();

        String step = m.group(4);
        if(step!=null) {
            out.start("step");
            parseExpr(step);
            out.end();
        }
        return null;
    })).or(parser(DOWHILE, m -> {
        out.start("do").start("while");
        return parseExpr(m.group(1));
    })).or(parser(ENDDO, m -> {
        out.end();
        return null;
    })).or(parser(CONTINUE, m -> {
        out.empty("continue");
        return null;
    })).or(expr);

    // start parsing format
    final Parser fmStart = parser(STAR, m->{
        out.empty("fmt");
        return parseExpr(m.group(1));
    }).or(parser(CONST, m->{
        out.start("fmt").attribute("label",m.group(1)).end();
        return parseExpr(m.group(5));
    })).or(parser(FMTBEG, m->{
        out.start("fmt");
        return fmp(m.group(1));
    })).or(expr);

    final Parser fmp = parser(SPACE, m -> {
        out.text(m.group(1));
        return m.group(2);              // continue
    }).or(parser(SEP, m -> {
        out.empty("sep");
        return m.group(1);              // continue
    })).or(line-> {
        if (line.charAt(0) == '/') {
            out.empty("nl");
            return fmp(line.substring(1));
        }
        return line;
    }).or(parser(FMREP, m->{
        out.start("fmt").attribute("rep", m.group(1));
        return fmp(m.group(2));
    })).or(parser(FMP, m -> {
        out.ltext("fmp", m.group(1));
        return fmp(m.group(2));
    })).or(parser(STRING1, m -> {
        out.text("string", m.group(1));
        return fmp(m.group(2));
    })).or(parser(STRING2, m -> {
        out.text("string", m.group(1));
        return fmp(m.group(2));
    })).or(parser(FMTEND, m->{
        out.end();
        return parseExpr(m.group(1)); // stop parsing patterns
    })).or(parser(BRCLOSE, m -> {
        out.end();
        return fmp(m.group(1));
    }));

    // parse format patterns
    private String fmp(String line) {
        current = fmp;
        return fmp.parse(line);
    }

    // current parser to continue
    Parser current = main;

    String startFmt(String line)  {
        // to continue with
        return fmStart.parse(line);
    }

    String parseExpr(String line) {
        current = expr;
        return expr.parse(line);
    }

    public String parse(String line) {
        return current.parse(line);
    }

    public CodeParser(Writer out) {
        super(out);
    }
}