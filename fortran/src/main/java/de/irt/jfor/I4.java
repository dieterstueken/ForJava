package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:52
 * modified by: $Author$
 * modified on: $Date$
 */
public class I4 implements Ref {

    public int v = 0;

    public I4(int v) {
        this.v = v;
    }

    public String toString() {
        return Integer.toString(v);
    }

    public static I4 of() {
        return new I4((short)0);
    }

    public static I4 of(int value) {
        return new I4((short)value);
    }
}
