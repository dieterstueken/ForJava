package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 14:17
 * modified by: $Author$
 * modified on: $Date$
 */
public class ChArr implements Arr {

    public final StringBuffer buffer;

    public ChArr(int len) {
        buffer = new StringBuffer(len);
        while(len-->0)
            buffer.append(' ');
    }

    public ChArr(String value) {
        buffer = new StringBuffer(value);
    }

    public ChArr() {
        buffer = new StringBuffer();
    }

    public String toString() {
        return buffer.toString();
    }

    public char get(int index) {
        return buffer.charAt(index);
    }

    public char set(int index, char ch) {
        buffer.setCharAt(index, ch);
        return ch;
    }

    public static ChArr of() {
        return new ChArr();
    }

    public static ChArr of(int len) {
        return new ChArr(len);
    }

    public ChArr substring(int i, int len) {
        len = Math.min(len, buffer.length());
        return new ChArr(toString().substring(i, i+len));
    }
}
