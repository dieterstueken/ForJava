package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 14:01
 * modified by: $Author$
 * modified on: $Date$
 */
public class Fortran {


    public static float min(double v1, double v2) {
        return (float) Math.min(v1, v2);
    }

    public static short min0(double v1, double v2) {
        return (short) Math.min(v1, v2);
    }

    public static float min1(float v1, float v2) {
        return Math.min(v1, v2);
    }


    public static float max(double v1, double v2) {
        return (float) Math.max(v1, v2);
    }

    public static short max0(double v1, double v2) {
        return (short) Math.max(v1, v2);
    }

    public static short amax0(float v1, float v2) {
        return (short) Math.max(v1, v2);
    }

    public static float amax1(float v1, float v2) {
        return  Math.max(v1, v2);
    }


    public static float abs(float value) {
        return  Math.abs(value);
    }

    public static short iabs(int v) {
        return (short) Math.abs(v);
    }

    public static float cabs(Complex c) {
        return (float) c.abs();
    }


    public static float sqrt(double value) {
        return (float) Math.sqrt(value);
    }

    public static Complex csqrt(Complex value) {
        return  value.sqrt();
    }

    public static float dsqrt(float v) {
        return (float) Math.sqrt(v);
    }


    public static float sin(double value) {
        return (float) Math.sin(value);
    }

    public static float asin(float v) {
        return (float) Math.asin(v);
    }


    public static float cos(float v) {
        return (float) Math.cos(v);
    }

    public static float acos(float value) {
        return (float) Math.acos(value);
    }


    public static float tan(double value) {
        return (float) Math.tan(value);
    }

    public static float atan(float v) {
        return (float) Math.atan(v);
    }


    public static float tanh(double value) {
        return (float) Math.tanh(value);
    }

    public static float atan2(float v1, float v2) {
        return (float) Math.atan2(v1, v2);
    }

    public static float datan2(float v1, float v2) {
        return (float) Math.atan2(v1, v2);
    }


    public static float exp(float v) {
        return (float) Math.exp(v);
    }

    public static float dexp(float v) {
        return (float) Math.exp(v);
    }


    public static float alog(float value) {
        return (float) Math.log(value);
    }

    public static float dlog(float v) {
        return (float) Math.log(v);
    }

    public static float alog10(float value) {
        return (float) Math.log10(value);
    }

    public static float dlog10(float v) {
        return (float) Math.log10(v);
    }


    public static short nint(double value) {
        return (short) Math.round(value);
    }

    public static short idint(double v) {
        return (short) v;
    }

    public double dble(float value) {
        return value;
    }

    public static Complex cmplx(double re, double im) {
        return Complex.of(re, im);
    }


    public static short ifix(double v) {
        return (short) (v - idint(v));
    }

    public static String len_trim(String str) {
        return str.trim();
    }
}
