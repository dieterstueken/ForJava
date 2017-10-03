package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:56
 */
public class Name extends Named {

    static final Toker toker = toker("(\\w+)", m -> new Name(m.group(1)));

    Name(String name) {
        super(name);
    }
}
