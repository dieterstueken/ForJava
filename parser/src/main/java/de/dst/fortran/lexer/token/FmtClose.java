package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.10.17
 * Time: 19:18
 */
public class FmtClose extends Token {

    static final FmtClose token = new FmtClose();

    static final Toker toker = toker("\\)[\"']", m -> token);
}