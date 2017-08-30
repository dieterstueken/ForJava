package de.irt.fortran;


/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.08.17
 * Time: 16:29
 */
public class F77 {

    final Units units;

    public F77(Units units) {
        this.units = units;
    }

    protected <C extends Common> C common(Class<C> type) {
        return units.common(type);
    }

    public static int nint(float d) {
        return Math.round(d);
    }

    public static float max(float f1, float f2) {
        return Math.max(f1,f2);
    }

    public static float min(float f1, float f2) {
            return Math.min(f1,f2);
        }
}
