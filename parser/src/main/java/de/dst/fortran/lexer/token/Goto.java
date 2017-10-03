package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:56
 */
public class Goto extends Named {

    static final Toker toker = toker("goto\\s+(\\d+)", m -> new Goto(m.group(1)));

    Goto(String name) {
        super(name);
    }
}
