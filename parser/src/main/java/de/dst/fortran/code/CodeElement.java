package de.dst.fortran.code;

import org.w3c.dom.Element;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  08.01.2019 12:44
 * modified by: $Author$
 * modified on: $Date$
 */
public interface CodeElement extends Context {

    Code code();

    Element element();

    String getLine();

    default String getName() {
        return code().getName();
    }
}
