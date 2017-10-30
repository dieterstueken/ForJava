package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:54
 * modified by: $Author$
 * modified on: $Date$
 */
public class Complex {

    public double re = 0;

    public double im = 0;

    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public Complex assign(double re, double im) {
        this.re = re;
        this.im = im;
        return this;
    }

    public Complex assign(Complex o) {
        return assign(o.re, o.im);
    }

    public String toString() {
        return String.format("Cplx(%d,%d)", re, im);
    }

    public static Complex of() {
        return of(0,0);
    }

    public static Complex of(double re, double im) {
        return new Complex(re, im);
    }

    public double abs() {
        return Math.hypot(re, im);
    }

    public Complex add(Complex o) {
        return new Complex(re + o.re, im + o.im);
    }

    public Complex sub(Complex o) {
        return new Complex(re - o.re, im - o.im);
    }

    public Complex mul(Complex o) {
        return new Complex(re * o.re - im * o.im, re * o.im + im * o.re);
    }

    public Complex div(Complex o) {
        double d = o.re*o.re + o.im*o.im;
        return new Complex((re * o.re + im * o.im)/d, (im * o.re + re * o.im)/d);
    }

    public Complex sqrt() {
        double r = abs();
        int sign = im>=0?1:-1;
        return of(Math.sqrt((re-r)/2), sign * Math.sqrt((re+r)/2));
    }
}
