package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:53
 * modified by: $Author$
 * modified on: $Date$
 */
public class I4Arr {

    public final int v[];

    public I4Arr(int len) {
        this.v = new int[len];
    }

    public int get(int index) {
        return v[index-1];
    }

    public int set(int index, int value) {
        v[index-1] = value;
        return value;
    }

    public String toString() {
        return "i4[]";
    }

    public static I4Arr of(int dim) {
        return new I4Arr(dim);
    }

}
