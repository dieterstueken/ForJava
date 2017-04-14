package de.dst.fortran.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.04.17
 * Time: 15:27
 */
public class CodeParser extends Parser {

    static final Pattern SUBROUTINE = Pattern.compile("subroutine\\s*(\\w+)\\s*(.*)");
    static final Pattern FUNCTION = Pattern.compile("(\\w+\\S*)?\\s*function\\s*(\\w+)\\s*(.*)");
    static final Pattern BLOCKDATA = Pattern.compile("block\\s+data\\s+(\\w+)\\s*");
    static final Pattern DATA = Pattern.compile("data\\s+(\\w+)\\s*");
    static final Pattern DIM = Pattern.compile("((integer|real|character)(\\*\\d+)?)(.*)");
    static final Pattern COMMON = Pattern.compile("common\\s*/(\\w+)/\\s*(.*)");
    static final Pattern BOOLEAN = Pattern.compile("\\.(eq|ne|le|lt|ge|gt|and|or)\\.");
    static final Pattern ASSIGN = Pattern.compile("(\\w+[^⁼]*)=(.*)");
    static final Pattern BOP = Pattern.compile(":(eq|ne|le|lt|ge|gt|and|or):(.*)");
    static final Pattern IF = Pattern.compile("if\\s*\\((.*)");
    static final Pattern THEN = Pattern.compile("then\\s*");
    static final Pattern ELSE = Pattern.compile("else\\s*");
    static final Pattern ELSEIF = Pattern.compile("else\\s+if\\s*\\((.*)");
    static final Pattern ENDIF = Pattern.compile("end\\s*if\\s*");
    static final Pattern DO = Pattern.compile("do\\s+(\\w+)\\s*=([^,]*),\\s*([^,]*)\\s*(,.*)?");
    static final Pattern DOWHILE = Pattern.compile("do\\s+while\\s*\\((.*)");
    static final Pattern ENDDO = Pattern.compile("end\\s*do\\s*");
    static final Pattern CONTINUE = Pattern.compile("continue\\s*");
    static final Pattern NAME = Pattern.compile("(\\w+)(.*)");
    static final Pattern FUN = Pattern.compile("(\\w+)\\s*\\((.*)");
    static final Pattern CALL = Pattern.compile("call\\s+(.*)");
    static final Pattern GOTO = Pattern.compile("goto\\s+(\\d+)");
    static final Pattern READ = Pattern.compile("read\\s*\\(\\s*(\\d+),\\s*\\*(,\\s*end=(\\d+))?\\)\\s+(.*)");
    static final Pattern RW = Pattern.compile("(read|write)\\s*\\(\\s*(\\d+),\\s*'\\((.*)");
    static final Pattern BRCLOSE = Pattern.compile("\\)(.*)");
    static final Pattern BROPEN = Pattern.compile("\\((.*)");
    static final Pattern OPENF = Pattern.compile("open\\((\\d),\\s*file=\"([^\\)]*)\"\\)\\s*");
    static final Pattern CLOSE = Pattern.compile("close\\((\\d)\\)\\s*");

    //static final Pattern FMOPEN = Pattern.compile("'\\((.*)");
    static final Pattern FMCLOSE = Pattern.compile("\\)'\\)(.*)");
    static final Pattern CONST = Pattern.compile("((\\.\\d+)|(\\d+(\\.\\d*)?))(.*)");
    static final Pattern STRING = Pattern.compile("\"([^\"]*)\"(.*)");
    static final Pattern SEP = Pattern.compile(",(.*)");
    static final Pattern END = Pattern.compile("end\\s*");
    static final Pattern RETURN = Pattern.compile("return\\s*");
    static final Pattern SPACE = Pattern.compile("(\\s+)(.*)");

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

    private void fi() {
        // terminate pending if
        if(!then)
            out.end();
    }

    void finish(Runnable finish) {
        if(this.finish==null)
            this.finish = finish;
        else {
            // cascade
            Runnable other = this.finish;
            this.finish = () -> {
                finish.run();
                other.run();
            };
        }
    }

    // start a new code line
    public void nl() {
        Runnable f = this.finish;
        this.finish = null;
        if(f!=null)
            f.run();
        out.nl();
    }

    Parser expr = parser(line -> (line == null || line.isEmpty())
    ).or(parser(SPACE, m -> {
        out.text(m.group(1));
        return parseExpr(m.group(2));
    })).or(parser(CALL, m -> {
        out.start("call");
        finish(out::end);
        return parseExpr(m.group(1));
    })).or(parser(OPENF, m -> {
        out.start("open")
                .attribute("ch", m.group(1))
                .attribute("file", m.group(2))
                .end();
        return true;
    })).or(parser(CLOSE, m -> {
        out.start("close")
                .attribute("ch", m.group(1))
                .end();
        return true;
    })).or(parser(READ, m -> {
        out.start("read")
                .attribute("ch", m.group(1))
                .attribute("end", m.group(3));
        finish(out::end);
        return parseExpr(m.group(4));
    })).or(parser(RW, m -> {
        out.start(m.group(1))
                .attribute("ch", m.group(2))
                .start("fmt");
        finish(out::end);
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
    })).or(parser(FMCLOSE, m -> {
        out.end();
        return parseExpr(m.group(1));
    })).or(parser(BRCLOSE, m -> {
        out.end();
        return parseExpr(m.group(1));
    })).or(parser(STRING, m -> {
        out.text("str", m.group(1));
        return parseExpr(m.group(2));
    })).or(parser(BOP, m -> {
        out.empty(m.group(1));
        return parseExpr(m.group(2));
    })).or(parser(line -> {
        String op = op(line.charAt(0));
        if (op != null) {
            out.empty(op);
            return parseExpr(line.substring(1));
        }
        return false;
    })).or(parser(SEP, m -> {
        out.empty("sep");
        return parseExpr(m.group(1));
    })).or(parser(GOTO, m -> {
        out.text("goto", m.group(1));
        return true;
    })).or(parser(RETURN, m -> {
        out.empty("return");
        return true;
    })).or(parser(THEN, m -> {
        if (!then) {
            out.start("then");
            then = true;
        }
        return true;
    })).or(parser(FUN, m -> {
        String name = m.group(1);
        out.start("fun")
                .attribute("name", name);
        return parseExpr(m.group(2));
    })).or(parser(CONST, m -> {
        String name = m.group(1);
        out.text("val", name);
        return parseExpr(m.group(5));
    })).or(parser(NAME, m -> {
        String name = m.group(1);
        out.text("var", name);
        return parseExpr(m.group(2));
    })).or(parser(code -> {
        out.text("code", code);
        return true;
    }));

    Parser main = parser(SPACE, m->{
        out.text(m.group(1));
        return parseLine(m.group(2));
    }).or(parser(SUBROUTINE, m -> {
        out.start("block")
                .attribute("type", "subroutine")
                .attribute("name", m.group(1));
        return parseExpr(m.group(2));
    })).or(parser(FUNCTION, m -> {
        out.start("block")
                .attribute("type", "function")
                .attribute("return", m.group(1))
                .attribute("name", m.group(2));
        return parseExpr(m.group(3));
    })).or(parser(BLOCKDATA, m -> {
        out.start("block")
                .attribute("type", "data")
                .attribute("name", m.group(1));
        return true;
    })).or(parser(DATA, m -> {
        out.start("data")
                .attribute("name", m.group(1));
        finish(out::end);
        return true;
    })).or(parser(COMMON, m -> {
        out.start("common")
                .attribute("name", m.group(1));
        finish(out::end);
        parseExpr(m.group(2));
        return true;
    })).or(parser(DIM, m -> {
        out.start("dim").attribute("type", m.group(1));
        finish(out::end);
        return parseExpr(m.group(4));
    })).or(parser(END, m -> {
        out.end();
        return true;
    })).or(parser(IF, m -> {
        out.start("if").start("cond");
        then = false;
        finish(this::fi); // close if inline if
        return parseExpr(m.group(1));
    })).or(parser(ELSE, m -> {
        out.end().start("else");
        then = false;
        return true;
    })).or(parser(ELSEIF, m -> {
        out.end().start("elif").start("cond");
        then = true; // don't open a then
        return parseExpr(m.group(1));
    })).or(parser(ENDIF, m -> {
        // (then|else|elif) endif
        out.end().end();
        return true;
    })).or(parser(DO, m -> {
        out.start("do")
                .attribute("var", m.group(1))
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
        return true;
    })).or(parser(DOWHILE, m -> {
        out.start("do").start("while");
        parseExpr(m.group(1));
        return true;
    })).or(parser(ENDDO, m -> {
        out.end();
        return true;
    })).or(parser(CONTINUE, m -> {
        out.empty("continue");
        return true;
    })).or(expr);

    public CodeParser(Writer out) {
        super(out);
    }

    boolean parseExpr(String line) {
        return expr.parse(line);
    }

    boolean parseLine(String line) {
        return main.parse(line);
    }

    public boolean parse(String line) {

        Matcher m = BOOLEAN.matcher(line);

        line = m.replaceAll(":$1:");

        return parseLine(line);
    }

}
