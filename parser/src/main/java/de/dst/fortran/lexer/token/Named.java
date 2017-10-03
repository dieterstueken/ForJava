package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:56
 */
abstract public class Named extends Token {

    final String name;

    Named(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("%s{%s}", getClass().getSimpleName(), name);
    }
}
