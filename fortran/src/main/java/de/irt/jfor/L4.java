package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:52
 * modified by: $Author$
 * modified on: $Date$
 */
public class L4 implements Ref {

    public boolean v = false;

    public L4(boolean v) {
        this.v = v;
    }

    public String toString() {
        return Boolean.toString(v);
    }

    public static L4 of() {
        return new L4(false);
    }
}
