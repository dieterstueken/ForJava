package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:56
 */
public class Call extends Token {

    static final Call token = new Call();

    static final Toker toker = toker("call\\s*", m -> token);
}
