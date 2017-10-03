package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Common extends Named {

    static final Toker toker = toker("common\\s*/(\\w+)/\\s*", m -> new Common(m.group(1)));

    Common(String name) {
        super(name);
    }
}
