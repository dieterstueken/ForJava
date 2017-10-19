package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 14:01
 * modified by: $Author$
 * modified on: $Date$
 */
public class Fortran {

    public static I2 i2() {
        return new I2((short)0);
    }

    public static I2 i2(short value) {
        return new I2(value);
    }

    public static I2 i2(int value) {
        return new I2((short)value);
    }

    public static L4 l4() {
        return new L4(false);
    }

    public static I4 i4() {
        return new I4((short)0);
    }

    public static I4 i4(int value) {
        return new I4((short)value);
    }

    public static R4 r4() {
        return new R4(0);
    }

    public static R4 r4(float val) {
        return new R4(val);
    }

    public static R4 r4(double val) {
        return new R4((float)val);
    }

    public static R8 r8() {
        return new R8(0);
    }

    public static R8 f8(double val) {
        return new R8((float)val);
    }

    public static Complex complex(double re, double im) {
        return new Complex(re, im);
    }

    public static Chars chars() {
        return new Chars();
    }

    public static Chars chars(int len) {
        return new Chars(len);
    }

    public static Ch ch() {
        return new Ch(' ');
    }
}
