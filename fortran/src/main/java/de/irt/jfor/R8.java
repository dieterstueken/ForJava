package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:53
 * modified by: $Author$
 * modified on: $Date$
 */
public class R8 implements Ref {

    public double v = 0;

    public R8(float v) {
        v = v;
    }

    public String toString() {
        return Double.toString(v);
    }

    public static R8 of() {
        return new R8(0);
    }

    public static R8 of(double val) {
        return new R8((float)val);
    }

}
