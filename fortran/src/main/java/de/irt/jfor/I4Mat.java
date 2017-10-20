package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:53
 * modified by: $Author$
 * modified on: $Date$
 */
public class I4Mat extends Mat {

    public final int v[];

    public I4Mat(int nx, int ny) {
        super(nx, ny);
        this.v = new int[nx*ny];
    }

    public int get(int ix, int iy) {
        return v[index(ix, iy)];
    }

    public int set(int ix, int iy, int value) {
        v[index(ix, iy)] = value;
        return value;
    }


    public String toString() {
        return "i4[][]";
    }

    public static I4Mat of(int n1, int n2) {
        return new I4Mat(n1, n2);
    }

}
