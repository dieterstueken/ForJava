package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:53
 * modified by: $Author$
 * modified on: $Date$
 */
public class I2Cub extends Cub {

    public final short v[];

    public I2Cub(int nx, int ny, int nz) {
        super(nx, ny, nz);
        this.v = new short[nx*ny*nz];
    }

    public short get(int ix, int iy, int iz) {
        return v[index(ix, iy, iz)];
    }

    public short set(int ix, int iy, int iz, short value) {
        v[index(ix, iy, iz)] = value;
        return value;
    }

    public int set(int ix, int iy, int iz, int value) {
        v[index(ix, iy, iz)] = (short) value;
        return value;
    }

    public String toString() {
        return "i2[][][]";
    }

    public static I2Cub of(int n1, int n2, int n3) {
        return new I2Cub(n1, n2, n3);
    }
}
