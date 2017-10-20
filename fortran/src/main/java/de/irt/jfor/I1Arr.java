package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:53
 * modified by: $Author$
 * modified on: $Date$
 */
public class I1Arr implements Arr {

    public final byte v[];

    public I1Arr(int len) {
        this.v = new byte[len];
    }

    public short get(int index) {
        return v[index-1];
    }

    public short set(int index, byte value) {
        v[index-1] = value;
        return value;
    }

    public int set(int index, char value) {
        v[index-1] = (byte) value;
        return value;
    }

    public int set(int index, int value) {
        v[index-1] = (byte) value;
        return value;
    }

    public String toString() {
        return "i2[]";
    }


    public static I1Arr of(int dim) {
        return new I1Arr(dim);
    }
}
