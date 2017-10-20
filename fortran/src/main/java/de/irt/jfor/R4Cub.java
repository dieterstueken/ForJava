package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:53
 * modified by: $Author$
 * modified on: $Date$
 */
public class R4Cub extends Cub {

    public final float v[];

    public R4Cub(int nx, int ny, int nz) {
        super(nx, ny, nz);
        this.v = new float[nx*ny*nz];
    }

    public float get(int ix, int iy, int iz) {
        return v[index(ix, iy, iz)];
    }

    public float set(int ix, int iy, int iz, float value) {
        v[index(ix, iy, iz)] = value;
        return value;
    }

    public double set(int ix, int iy, int iz, double value) {
        v[index(ix, iy, iz)] = (float) value;
        return value;
    }

    public String toString() {
        return "r4[][][]";
    }


    public static R4Cub of(int n1, int n2, int n3) {
        return new R4Cub(n1, n2, n3);
    }
}
