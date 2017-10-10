package de.dst.fortran.lexer.item;

import de.dst.fortran.lexer.LineBuffer;
import org.intellij.lang.annotations.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 06.10.17
 * Time: 11:29
 */
public enum Item {
    SPACE(expr("(\\s+)")),

    FUNCTION(expr("((?:integer|real|complex|character|logical)(?:\\*(:?\\d+))?\\s)?\\s*function\\s*(\\w+)\\s*")),
    SUBROUTINE(expr("subroutine\\s*(\\w+)\\s*")),
    BLOCKDATA(expr("block\\s+data\\s+(\\w+)\\s*")),
    PROGRAM(expr("program\\s+(\\w+)\\s*")),
    RETURN("return"),
    STOP("stop"),

    LOGICAL(expr(":(eq|ne|le|lt|ge|gt|and|or):")),
    SEP(","),
    RANGE(":"),
    WILDCARD("(*)"),

    FORMAT(expr("format\\s*\\(\\s*")),
    FMTOPEN(expr("['\"]\\(")),
    FMTCLOSE(expr("\\)[\"']")),

    COMMON(expr("common\\s*/(\\w+)/\\s*")),
    DIM(expr("((?:integer|real|complex|character|logical)(?:\\*(:?(:?\\(\\*\\)|\\d+)?))?)")),
    ALLOCATABLE(expr("allocatable\\s*::")),
    DATA("data "),

    IF(expr("if\\s*\\(")),
    THEN("then"),
    ELSEIF(expr("else\\s*if\\s*\\(")),
    ELSE("else"),
    ENDIF(expr("end\\s*if\\s*")),

    DO(expr("do\\s*(\\w+)\\s*=")),
    DOWHILE(expr("do\\s+while\\s*\\(")),
    ENDDO(expr("end\\s*do\\s*")),

    GOTO(expr("goto\\s+(\\d+)")),
    CALL(expr("call\\s*(\\w*)\\s*")),
    CONTINUE("continue"),

    USE(expr("use\\s*(.*)")),
    ALLOCATE(expr("allocate\\s*\\(\\s*(\\w+)\\(")),
    DEALLOCATE(expr("deallocate\\s*\\(\\s*(\\w+)")),
    FIO(expr("(open|close|read|write|rewind)\\s*\\(")),
    FPRINT(expr("print\\s*\\*,")),
    FMT(expr("(\\d*[ailtfx](?:\\d+(?:\\.\\d+)?)?)")),
    //FMT(expr("((?:\\d*f\\d+\\.\\d+)|(?:\\d*a\\d+))")),
    FMTREP(expr("(\\d+)\\s*\\(\\s*")),
    FSTAT(expr("(iostat|stat|status|err|file|form|access|rec|recl|end)\\s*=")),

    TEXT(expr("((?:'[^\']*')|(?:\"[^\"]*\"))")) {
        @Override
        Token token(Matcher m) {
            String text = m.group();
            // strip quotes
            return token(text.substring(1, text.length()-1));
        }
    },
    CONST(expr("((?:\\.\\d+)|(?:\\d+(?:\\.\\d*)?))([de]-?\\d+)?")),
    BOOLEAN(expr("\\.(true|false)\\.")),
    ASSIGN("="),

    END("end"),
    APPLY(expr("(\\w+)\\s*\\(")),
    NAME(expr("(\\w+)")),
    BINOP(expr("(\\+|-|\\*\\*?|//?)")),

    OPEN("("),
    CLOSE(")"),

    COMMENT(expr("!(.*)")),

    LABEL(),
    LINUM(),
    CODELINE(),
    CONTLINE(),
    ENDLINE(),
    COMMENTLINE(),

    FILE(),
    ENDFILE();

    Item() {
        this.tokenizer = null;
    }

    Item(String pattern) {
        Token token = token();
        this.tokenizer = line -> line.matches(pattern) ? token : null;

    }

    Item(Pattern pattern) {
        Matcher m = pattern.matcher("");
        this.tokenizer = line -> line.matches(m) ? token(m) : null;
    }

    static Pattern expr(@Language("regexp") String expr) {
        return Pattern.compile(expr, Pattern.CASE_INSENSITIVE);
    }

    final Function<LineBuffer, Token> tokenizer;

    public Token token() {
        return new Token(this);
    }

    public Token token(String value) {
        return new Token(this) {

            @Override
            public String get(int index) {
                if(index==0)
                    return value;
                else
                    return super.get(index);
            }

            @Override
            public int size() {
                return 1;
            }

            @Override
            public String toString() {
                return String.format("%s: %s", item.toString(), value);
            }
        };
    }

    Token token(Matcher m) {
        if(!m.lookingAt())
            return null;

        int count = m.groupCount();

        if(count<=0)
            return token();

        if(count==1)
            return token(m.group(1));

        List<String> values = new ArrayList<>(count);
        for(int i=0; i<count; ++i)
            values.add(m.group(i+1));

        return new Token(this) {
            @Override
            public String get(int index) {
                        return values.get(index);
                    }

            @Override
            public int size() {
                        return values.size();
                    }

            @Override
            public String toString() {
                StringBuilder buffer = new StringBuilder();
                char sep = '{';

                buffer.append(item.toString());
                for (String value : values) {
                    buffer.append(sep);
                    buffer.append(value);
                    sep = ',';
                }

                buffer.append('}');
                 
                return buffer.toString();
            }
        };
    }
}
