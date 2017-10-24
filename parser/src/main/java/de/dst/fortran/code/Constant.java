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

    public static Constant of(String text) {
        Number val = null;

        if(text.indexOf('.')<0) {
            val = Integer.decode(text);
        } else {
            int i = text.indexOf('D');
            if(i>=0) {
                text = text.substring(0, i) + "E" + text.substring(i+1);
                val = Double.parseDouble(text);
            } else
                val = Float.parseFloat(text);
        }

        return new Constant(val);
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
