package de.irt.fortran;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.08.17
 * Time: 16:32
 */
public class Units {

    final Map<Class<? extends Common>, Common> commons = new HashMap<>();
    final Map<Class<? extends F77>, F77> units = new HashMap<>();

    public <C extends Common> C common(Class<C> type) {
        Common c = commons.computeIfAbsent(type, Units::newCommon);
        return type.cast(c);
    }

    public <F extends F77> F unit(Class<F> type) {
        F77 unit = units.computeIfAbsent(type, this::newUnit);
        return type.cast(unit);
    }

    private static <C extends Common> C newCommon(Class<C> type) {
        try {
            return type.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private <U extends F77> U newUnit(Class<U> type) {
        try {
            Constructor<U> constr = type.getConstructor(Units.class);
            return constr.newInstance(units);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
