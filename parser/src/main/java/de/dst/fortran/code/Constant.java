package de.dst.fortran.code;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  07.02.13 19:42
 * modified by: $Author$
 * modified on: $Date$
 */
public class Constant implements Value {

    public final Number value;

    public Constant(Number value) {
        this.value = value;
    }

    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Constant)) return false;

        Constant constant = (Constant) o;

        return value.equals(constant.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
