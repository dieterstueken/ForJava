package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Const extends Valued {

    static final Toker toker = toker("-?((\\.\\d+)|(\\d+(\\.\\d*)?))", m -> new Const(m.group()));

    Const(String value) {
        super(value);
    }
}
