package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:53
 * modified by: $Author$
 * modified on: $Date$
 */
public class R4Arr {

    public final float v[];

    public R4Arr(int len) {
        this.v = new float[len];
    }

    public float get(int index) {
        return v[index-1];
    }

    public float set(int index, float value) {
        v[index-1] = value;
        return value;
    }

    public double set(int index, double value) {
        v[index-1] = (short) value;
        return value;
    }

    public String toString() {
        return "r4[]";
    }


    public static R4Arr of(int dim) {
        return new R4Arr(dim);
    }

}
