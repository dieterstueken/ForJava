package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Fmt extends Named {

    static final Toker toker = toker("(\\d+) *\\( *(.*)", m -> new Fmt(m.group(1)));

    Fmt(String name) {
        super(name);
    }
}
