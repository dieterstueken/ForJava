package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.10.17
 * Time: 19:18
 */
public class Close extends Token {

    static final Close token = new Close();

    static final Toker toker = toker("\\)", m -> token);
}