package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Return extends Token {

    static final Return token = new Return();

    static final Toker toker = toker("return\\s*", m -> token);
}
