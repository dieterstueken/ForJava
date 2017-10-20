package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 14:17
 * modified by: $Author$
 * modified on: $Date$
 */
public class Ch {

    public final char c;

    public Ch(char c) {
        this.c = c;
    }

    public String toString() {
        return Character.toString(c);
    }

    public static Ch of() {
            return new Ch(' ');
        }
}
