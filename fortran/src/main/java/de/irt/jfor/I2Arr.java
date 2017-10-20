package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:53
 * modified by: $Author$
 * modified on: $Date$
 */
public class I2Arr {

    public final short v[];

    public I2Arr(int len) {
        this.v = new short[len];
    }

    public short get(int index) {
        return v[index-1];
    }

    public short set(int index, short value) {
        v[index-1] = value;
        return value;
    }

    public int set(int index, int value) {
        v[index-1] = (short) value;
        return value;
    }

    public String toString() {
        return "i2[]";
    }


    public static I2Arr of(int dim) {
        return new I2Arr(dim);
    }
}
