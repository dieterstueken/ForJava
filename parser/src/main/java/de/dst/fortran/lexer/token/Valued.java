package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:56
 */
abstract public class Valued extends Token {

    final String value;

    Valued(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("%s{%s}", getClass().getSimpleName(), value);
    }
}
