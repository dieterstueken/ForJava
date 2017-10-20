package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 14:17
 * modified by: $Author$
 * modified on: $Date$
 */
public class ChArr {

    public final StringBuffer buffer;

    public ChArr(int len) {
        buffer = new StringBuffer(len);
    }

    public ChArr() {
        buffer = new StringBuffer();
    }

    public String toString() {
        return buffer.toString();
    }


    public static ChArr of() {
        return new ChArr();
    }

    public static ChArr of(int len) {
        return new ChArr(len);
    }
}
