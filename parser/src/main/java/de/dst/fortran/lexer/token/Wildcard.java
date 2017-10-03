package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Wildcard extends Token {

    static final Wildcard token = new Wildcard();

    static final Toker toker = toker("\\*", m -> token);
}
