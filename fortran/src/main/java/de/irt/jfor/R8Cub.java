package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:53
 * modified by: $Author$
 * modified on: $Date$
 */
public class R8Cub extends Cub {

    public final double v[];

    public R8Cub(int nx, int ny, int nz) {
        super(nx, ny, nz);
        this.v = new double[nx*ny*nz];
    }

    public double get(int ix, int iy, int iz) {
        return v[index(ix, iy, iz)];
    }

    public double set(int ix, int iy, int iz, double value) {
        v[index(ix, iy, iz)] = value;
        return value;
    }

    public String toString() {
        return "r8[][][]";
    }


    public static R8Cub of(int n1, int n2, int n3) {
        return new R8Cub(n1, n2, n3);
    }
}
