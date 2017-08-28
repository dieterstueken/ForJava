package de.dst.fortran.code;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  07.02.13 19:43
 * modified by: $Author$
 * modified on: $Date$
 */
public interface Value {

    Value UNDEF = new Value() {

        @Override
        public String toString() {
            return "UNDEF";
        }
    };
}
