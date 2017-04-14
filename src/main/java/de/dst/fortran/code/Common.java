package de.dst.fortran.code;

import java.util.function.Function;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  07.02.13 18:48
 * modified by: $Author$
 * modified on: $Date$
 */
public class Common extends Entity implements Context {

    public final Entities<Variable> members;

    public Common(String common, Function<String, Variable> newVariable) {
        super(common);

        members = new Entities<>(name -> newVariable.apply(name).context(this));
    }
}
