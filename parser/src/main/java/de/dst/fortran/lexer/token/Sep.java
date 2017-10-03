package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Sep extends Token {

    static final Sep token = new Sep();

    static final Toker toker = toker(",", m -> token);
}
