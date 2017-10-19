package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 14:17
 * modified by: $Author$
 * modified on: $Date$
 */
public class Chars {

    public final StringBuffer buffer;

    public Chars(int len) {
        buffer = new StringBuffer(len);
    }

    public Chars() {
        buffer = new StringBuffer();
    }

    public String toString() {
        return buffer.toString();
    }
}
