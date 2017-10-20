package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:53
 * modified by: $Author$
 * modified on: $Date$
 */
public class R8Arr implements Arr {

    public final double v[];

    public R8Arr(int len) {
        this.v = new double[len];
    }

    public double get(int index) {
        return v[index-1];
    }

    public double set(int index, double value) {
        v[index-1] = value;
        return value;
    }

    public String toString() {
        return "r8[]";
    }


    public static R8Arr of(int dim) {
        return new R8Arr(dim);
    }

}
