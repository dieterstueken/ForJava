package de.irt.jfor;

import java.lang.reflect.Constructor;
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

    protected Unit(Unit parent) {
        this.units = parent.units;
    }

    @SuppressWarnings("unchecked")
    protected <C extends Common> C common(Class<C> type) {
        return (C) units.computeIfAbsent(type, this::createUnit);
    }

    @SuppressWarnings("unchecked")
    protected <C extends Unit> C unit(Class<C> type) {
        return (C) units.computeIfAbsent(type, this::createUnit);
    }

    private <F extends Fortran> Fortran createUnit(Class<F> type) {
        try {
            Constructor<F> create = type.getConstructor(Unit.class);
            return create.newInstance(this);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private <F extends Fortran> Fortran createCommon(Class<F> type) {
        try {
            type.newInstance();
            Constructor<F> create = type.getConstructor();
            return create.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
