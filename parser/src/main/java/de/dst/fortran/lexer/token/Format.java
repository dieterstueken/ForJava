package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Format extends Token {

    static final Format token = new Format();

    static final Toker toker = toker("format\\s*\\(\\s*", m -> token);
}
