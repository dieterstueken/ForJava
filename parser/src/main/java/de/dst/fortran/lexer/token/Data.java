package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Data extends Named {

    static final Toker toker = toker("data\\s+(\\w+)\\s*", m -> new Data(m.group(1)));

    Data(String name) {
        super(name);
    }
}
