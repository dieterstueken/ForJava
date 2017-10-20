package de.irt.jfor;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 20.10.17
 * Time: 11:54
 */
public class Cub extends Mat {

    public final int nz;

    public Cub(int nx, int ny, int nz) {
        super(nx, ny);
        if(nz<1)
            throw new IllegalArgumentException("invalid  array index");
        this.nz = nz;
    }

    protected int index(int ix, int iy, int iz) {
        return index(ix, iy) + nx*ny * bound(iz, nz, "nz");
    }
}
