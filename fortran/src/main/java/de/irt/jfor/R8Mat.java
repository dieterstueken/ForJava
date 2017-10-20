package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:53
 * modified by: $Author$
 * modified on: $Date$
 */
public class R8Mat extends Mat {

    public final double v[];

    public R8Mat(int nx, int ny) {
        super(nx, ny);
        this.v = new double[nx*ny];
    }

    public double get(int ix, int iy) {
        return v[index(ix, iy)];
    }

    public double set(int ix, int iy, double value) {
        v[index(ix, iy)] = value;
        return value;
    }

    public String toString() {
        return "r8[][]";
    }


    public static R8Mat of(int n1, int n2) {
        return new R8Mat(n1, n2);
    }
}
