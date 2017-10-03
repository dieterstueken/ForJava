package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:56
 */
public class Apply extends Named {

    static final Toker toker = toker("(\\w+)\\s*\\(", m -> new Apply(m.group(1)));

    Apply(String name) {
        super(name);
    }
}
