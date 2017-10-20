package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:53
 * modified by: $Author$
 * modified on: $Date$
 */
public class I2 implements Ref {

    public short v = 0;

    public I2(short v) {
        this.v = v;
    }

    public String toString() {
        return Short.toString(v);
    }

    public static I2 of() {
        return new I2((short)0);
    }

    public static I2 of(short value) {
        return new I2(value);
    }

    public static I2 of(int value) {
        return new I2((short)value);
    }

}
