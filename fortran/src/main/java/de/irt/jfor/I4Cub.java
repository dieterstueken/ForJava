package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:53
 * modified by: $Author$
 * modified on: $Date$
 */
public class I4Cub extends Cub {

    public final int v[];

    public I4Cub(int nx, int ny, int nz) {
        super(nx, ny, nz);
        this.v = new int[nx*ny*nz];
    }

    public int get(int ix, int iy, int iz) {
        return v[index(ix, iy, iz)];
    }

    public int set(int ix, int iy, int iz, int value) {
        v[index(ix, iy, iz)] = value;
        return value;
    }

    public String toString() {
        return "i4[][][]";
    }

    public static I4Cub of(int n1, int n2, int n3) {
         return new I4Cub(n1, n2, n3);
    }
}
