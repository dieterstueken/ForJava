package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Fmtrep extends Valued {

    static final Toker toker = toker("(\\d+)\\s*\\(\\s*", m -> new Fmtrep(m.group(1)));

    Fmtrep(String value) {
        super(value);
    }
}
