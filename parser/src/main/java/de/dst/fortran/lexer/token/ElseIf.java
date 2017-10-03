package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class ElseIf extends Token {

    static final ElseIf token = new ElseIf();

    static final Toker toker = toker("else\\s+if\\s*\\(", m -> token);
}
