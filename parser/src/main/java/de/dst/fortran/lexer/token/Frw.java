package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Frw extends Named {

    static final Toker toker = toker("(read|write)\\s*\\(\\s+([^,]+\\s+)\\s,", m -> new Frw(m.group(1)));

    Frw(String name) {
        super(name);
    }
}
