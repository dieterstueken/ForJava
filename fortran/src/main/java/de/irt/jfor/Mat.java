package de.irt.jfor;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 20.10.17
 * Time: 11:54
 */
public class Mat {

    public final int nx;

    public final int ny;

    public Mat(int nx, int ny) {
        if(nx<1 || ny<1)
            throw new IllegalArgumentException("invalid  array index");

        this.nx = nx;
        this.ny = ny;
    }

    static int bound(int ix, int nx, String name) {
        if(ix<1 || ix>nx) {
            String error = String.format("index %s out of bounds %d: %d", name, ix, nx);
            throw new IndexOutOfBoundsException(error);
        }
        return ix-1;
    }

    protected int index(int ix, int iy) {
        return bound(ix, nx, "nx") + nx * bound(iy, ny, "ny");
    }
}
