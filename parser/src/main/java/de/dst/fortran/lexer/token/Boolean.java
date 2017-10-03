package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Boolean extends Valued {

    static final Toker toker = toker("\\.((true)|(false))\\.", m -> new Boolean(m.group()));

    Boolean(String value) {
        super(value);
    }
}
