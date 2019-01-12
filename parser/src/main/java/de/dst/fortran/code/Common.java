package de.dst.fortran.code;

import java.util.Collection;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  08.01.2019 12:33
 * modified by: $Author$
 * modified on: $Date$
 */
public interface Common extends Context {

    Collection<? extends Variable> members();
}
