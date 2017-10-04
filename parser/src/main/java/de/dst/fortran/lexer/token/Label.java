package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:56
 */
public class Label extends Token {

    final Integer label;

    public Label(Integer label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("%d:", label);
    }
}
