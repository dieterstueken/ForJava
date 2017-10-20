package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:53
 * modified by: $Author$
 * modified on: $Date$
 */
public class R4 {

    public float v = 0;

    public R4(float value) {
        v = value;
    }

    public String toString() {
        return Float.toString(v);
    }

    public static R4 of() {
        return new R4(0);
    }

    public static R4 of(float val) {
        return new R4(val);
    }

    public static R4 of(double val) {
        return new R4((float)val);
    }

}
