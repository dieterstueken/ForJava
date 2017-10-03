package de.dst.fortran.lexer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.10.17
 * Time: 19:48
 */
public class LineBuffer implements CharSequence {

    String line;

    public LineBuffer() {
        this("");
    }

    public LineBuffer(String line) {
        this.line = line;
        if(line==null)
            throw new NullPointerException("line is null");
    }

    @Override
    public String toString() {
        return line;
    }

    public int length() {
        return line.length();
    }

    @Override
    public char charAt(int index) {
        return line.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new LineBuffer(line.substring(start, end));
    }

    public boolean isEmpty() {
        return line.isEmpty();
    }

    public String line() {
        return line;
    }

    public LineBuffer eat(int len) {
        line = line.substring(len);
        return this;
    }
}
