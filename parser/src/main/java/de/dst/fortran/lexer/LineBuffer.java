package de.dst.fortran.lexer;

import java.util.regex.Matcher;

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

    public boolean matches(String item) {
        int len = item.length();

        if(line.length()<len)
            return false;

        if(!line.substring(0, item.length()).equalsIgnoreCase(item))
            return false;

        eat(item.length());
        return true;
    }

    public boolean matches(Matcher m) {
        m.reset(line);

        if(!m.lookingAt())
            return false;

        eat(m.group(0).length());

        return true;
    }
}
