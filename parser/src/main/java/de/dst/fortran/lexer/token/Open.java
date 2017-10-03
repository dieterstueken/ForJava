package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.10.17
 * Time: 19:18
 */
public class Open extends Token {

    static final Open token = new Open();

    static final Toker toker = toker("\\(", m -> token);
}