package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Fstat extends Named {

    static final Toker toker = toker("(iostat|status|err|file|form|access|recl)=", m -> new Fstat(m.group(1)));

    Fstat(String name) {
        super(name);
    }
}
