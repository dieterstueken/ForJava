package de.irt.jfor;

import com.sun.xml.internal.bind.v2.ClassFactory;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 14:27
 * modified by: $Author$
 * modified on: $Date$
 */
public class Unit extends Fortran {

    private final Map<Class<? extends Fortran>, Fortran> units;

    private <F extends Fortran> Fortran create(Class<F> type) {
        try {
            Constructor<F> create = type.getConstructor(Unit.class);
            return create.newInstance(this);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    protected Unit(Unit parent) {
        this.units = parent.units;
    }

    protected Unit() {
        this.units = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    protected <C extends Common> C common(Class<C> type) {
        return (C) units.computeIfAbsent(type, ClassFactory::create);
    }

    @SuppressWarnings("unchecked")
    protected <C extends Unit> C unit(Class<C> type) {
        return (C) units.computeIfAbsent(type, this::create);
    }
}
