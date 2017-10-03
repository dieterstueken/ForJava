package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class If extends Token {

    static final If token = new If();

    static final Toker toker = toker("if\\s*\\(", m -> token);
}
