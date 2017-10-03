package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Then extends Token {

    static final Then token = new Then();

    static final Toker toker = toker("then", m -> token);
}
