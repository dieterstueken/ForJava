package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Space extends Valued {

    static final Toker toker = toker("\\s+", m -> new Space(m.group()));

    Space(String value) {
        super(value);
    }
}
