package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class EndIf extends Token {

    static final EndIf token = new EndIf();

    static final Toker toker = toker("end\\s*if\\s*", m -> token);
}
