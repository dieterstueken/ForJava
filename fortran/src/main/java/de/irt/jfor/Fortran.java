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

    public static float amin0(double v1, double v2) {
        return (short) Math.min(v1, v2);
    }

    public static int amin0(int v1, int v2) {
        return  Math.min(v1, v2);
    }

    public static short amin1(short v1, short v2) {
        return (short) Math.min(v1, v2);
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


    public static float sign(float v, float s) {
        return (short) dsign(v, s);
    }

    public static int sign(int v, int s) {
        v = Math.abs(v);
        return (short) (s<0 ? -v : v);
    }

    public static short isign(short v, short s) {
        return (short) sign(v, s);
    }

    public static double dsign(double v, double s) {
        v = Math.abs(v);
        return (short) (s<0 ? -v : v);
    }

    public static float sqrt(float value) {
        return (float) Math.sqrt(value);
    }

    public static double sqrt(double value) {
        return Math.sqrt(value);
    }

    public static double dsqrt(double v) {
        return Math.sqrt(v);
    }

    public static Complex csqrt(Complex value) {
        return  value.sqrt();
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

    public static double datan(double v) {
        return Math.atan(v);
    }


    public static float tanh(double value) {
        return (float) Math.tanh(value);
    }

    public static float atan2(float v1, float v2) {
        return (float) Math.atan2(v1, v2);
    }

    public static double datan2(double v1, double v2) {
        return Math.atan2(v1, v2);
    }


    public static float exp(float v) {
        return (float) Math.exp(v);
    }

    public static double dexp(double v) {
        return Math.exp(v);
    }


    public static float pow(float v, float e) {
        return (float) Math.pow(v, e);
    }

    public static double pow(double v, double e) {
        return Math.pow(v, e);
    }


    public static float alog(float value) {
        return (float) Math.log(value);
    }

    public static double dlog(double v) {
        return Math.log(v);
    }

    public static float alog10(float value) {
        return (float) Math.log10(value);
    }

    public static double dlog10(double v) {
        return Math.log10(v);
    }


    public static short nint(double value) {
        return (short) Math.round(value);
    }

    public static short _int(double v) {
        return (short) v;
    }

    public static short idint(double v) {
        return (short) v;
    }

    public static float _float(short v) {
        return v;
    }

    public static float _float(double v) {
        return (short) v;
    }

    public double dble(float v) {
        return v;
    }

    public static Complex cmplx(double re, double im) {
        return Complex.of(re, im);
    }

    public static Complex cmplx(double re) {
        return cmplx(re, 0);
    }

    public static short ifix(double v) {
        return (short) (v - idint(v));
    }

    public static int len_trim(ChArr str) {
        return str.len_trim();
    }

    public Object ref(Object expr) {
        return expr;
    }
}
