package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Continue extends Token {

    static final Continue token = new Continue();

    static final Toker toker = toker("continue\\s*", m -> token);
}
