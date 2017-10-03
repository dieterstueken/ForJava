package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Binop extends Named {

    static final Toker toker = toker("(\\+|-|\\*\\*?|/)", m -> new Binop(m.group(1)));

    Binop(String name) {
        super(name);
    }
}
