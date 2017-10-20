package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:53
 * modified by: $Author$
 * modified on: $Date$
 */
public class I2Mat extends Mat {

    public final short v[];

    public I2Mat(int nx, int ny) {
        super(nx, ny);
        this.v = new short[nx*ny];
    }

    public short get(int ix, int iy) {
        return v[index(ix, iy)];
    }

    public short set(int ix, int iy, short value) {
        v[index(ix, iy)] = value;
        return value;
    }

    public int set(int ix, int iy, int value) {
        v[index(ix, iy)] = (short) value;
        return value;
    }

    public String toString() {
        return "i2[][]";
    }


    public static I2Mat of(int n1, int n2) {
        return new I2Mat(n1, n2);
    }
}
