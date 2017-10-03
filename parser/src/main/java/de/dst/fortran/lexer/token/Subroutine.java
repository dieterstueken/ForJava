package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Subroutine extends Named {

    static final Toker toker = toker("subroutine\\s*(\\w+)\\s*", m -> new Subroutine(m.group(1)));

    Subroutine(String name) {
        super(name);
    }
}
