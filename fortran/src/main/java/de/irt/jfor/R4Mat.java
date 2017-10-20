package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:53
 * modified by: $Author$
 * modified on: $Date$
 */
public class R4Mat extends Mat {

    public final float v[];

    public R4Mat(int nx, int ny) {
        super(nx, ny);
        this.v = new float[nx*ny];
    }

    public float get(int ix, int iy) {
        return v[index(ix, iy)];
    }

    public float set(int ix, int iy, float value) {
        v[index(ix, iy)] = value;
        return value;
    }

    public double set(int ix, int iy, double value) {
        v[index(ix, iy)] = (float) value;
        return value;
    }

    public String toString() {
        return "r4[][]";
    }

    public static R4Mat of(int n1, int n2) {
        return new R4Mat(n1, n2);
    }
}
