package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Text extends Valued {

    static final Toker toker = toker("'([^\']*)\'|\"([^\"]*)\"", m -> new Text(m.group()));

    Text(String value) {
        super(value);
    }
}
