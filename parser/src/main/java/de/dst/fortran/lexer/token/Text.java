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
        super(text(value));
    }

    static String text(String line) {
        return line.substring(1, line.length()-1);
    }
}
