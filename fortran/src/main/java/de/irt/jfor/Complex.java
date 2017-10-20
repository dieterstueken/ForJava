package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:54
 * modified by: $Author$
 * modified on: $Date$
 */
public class Complex implements Ref {

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

    public String toString() {
        return String.format("Cplx(%d,%d)", re, im);
    }

    public static Complex of() {
        return of(0,0);
    }

    public static Complex of(double re, double im) {
        return new Complex(re, im);
    }

    public Complex assign(Complex o) {
        return assign(o.re, o.im);
    }

    public Complex plus(Complex o) {
        return new Complex(re + o.re, im + o.im);
    }

    public Complex minus(Complex o) {
        return new Complex(re - o.re, im - o.im);
    }

    public Complex mul(Complex o) {
        return new Complex(re * o.re - im * o.im, re * o.im + im * o.re);
    }

    public Complex div(Complex o) {
        double d = o.re*o.re + o.im*o.im;
        return new Complex((re * o.re + im * o.im)/d, (im * o.re + re * o.im)/d);
    }
}
