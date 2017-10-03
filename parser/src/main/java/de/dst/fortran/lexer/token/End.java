package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class End extends Token {

    static final End token = new End();

    static final Toker toker = toker("end\\s*", m -> token);
}
