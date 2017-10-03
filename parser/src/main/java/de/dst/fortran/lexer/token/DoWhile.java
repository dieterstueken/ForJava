package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class DoWhile extends Token {

    static final DoWhile token = new DoWhile();

    static final Toker toker = toker("do\\s+while\\s*\\((.*)", m -> token);
}
