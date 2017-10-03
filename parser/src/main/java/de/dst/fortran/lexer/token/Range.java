package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Range extends Token {

    static final Range token = new Range();

    static final Toker toker = toker(":", m -> token);
}
