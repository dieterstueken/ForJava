package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:53
 * modified by: $Author$
 * modified on: $Date$
 */
public class I1Mat extends Mat {

    public final byte v[];

    public I1Mat(int nx, int ny) {
        super(nx, ny);
        this.v = new byte[nx*ny];
    }

    public byte get(int ix, int iy) {
        return v[index(ix, iy)];
    }

    public byte set(int ix, int iy, byte value) {
        v[index(ix, iy)] = value;
        return value;
    }

    public char set(int ix, int iy, char value) {
        v[index(ix, iy)] = (byte) value;
        return value;
    }

    public int set(int ix, int iy, int value) {
        v[index(ix, iy)] = (byte) value;
        return value;
    }

    public String toString() {
        return "i2[][]";
    }


    public static I1Mat of(int n1, int n2) {
        return new I1Mat(n1, n2);
    }
}
