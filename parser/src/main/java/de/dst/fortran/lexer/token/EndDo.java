package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class EndDo extends Token {

    static final EndDo token = new EndDo();

    static final Toker toker = toker("end\\s*do\\s*", m -> token);
}
