package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 14:17
 * modified by: $Author$
 * modified on: $Date$
 */
public class I1 implements Ref {

    public byte v = 0;

    public I1(char c) {
        this.v = (byte) c;
    }

    public String toString() {
        return Character.toString((char)v);
    }

    public static I1 of() {
            return new I1(' ');
        }
}
