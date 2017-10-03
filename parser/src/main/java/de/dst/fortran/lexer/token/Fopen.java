package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Fopen extends Named {

    static final Toker toker = toker("open\\s*\\(\\s+([^,]+\\s+)\\s,", m -> new Fopen(m.group(1)));

    Fopen(String name) {
        super(name);
    }
}
