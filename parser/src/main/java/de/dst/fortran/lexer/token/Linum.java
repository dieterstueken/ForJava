package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:56
 */
public class Linum extends Token {

    final int linum;

    public Linum(int linum) {
        this.linum = linum;
    }

    @Override
    public String toString() {
        return  String.format("line: %d", linum);
    }
}
