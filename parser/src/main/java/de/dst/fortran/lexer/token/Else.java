package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Else extends Token {

    static final Else token = new Else();

    static final Toker toker = toker("else", m -> token);
}
