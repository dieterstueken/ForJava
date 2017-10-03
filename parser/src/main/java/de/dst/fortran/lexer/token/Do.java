package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Do extends Named {

    static final Toker toker = toker("do\\s*(\\w+)\\s*=", m -> new Do(m.group(1)));

    Do(String name) {
        super(name);
    }
}
