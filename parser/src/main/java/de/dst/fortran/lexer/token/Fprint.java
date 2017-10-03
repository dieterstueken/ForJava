package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Fprint extends Token {

    static final Fprint token = new Fprint();

    static final Toker toker = toker("print\\s*\\*,", m -> token);
}
