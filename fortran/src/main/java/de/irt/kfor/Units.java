package de.irt.kfor;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class Units {

    private final Map<Class<?>, Object> units;

    public Units(Map<Class<?>, Object> units) {
        this.units = units;
    }

    protected Units() {
        this(new HashMap<>());
    }

    public <T> T unit(Class<T> type) {
        Object unit = units.computeIfAbsent(type, this::load);
        return type.cast(unit);
    }

    protected <T> T load(Class<T> type) {

        try {
            Constructor<T> create = type.getConstructor(Units.class);
            return create.newInstance(this);
        } catch (ReflectiveOperationException e) {
           // nect try
        }

        try {
            Constructor<T> create = type.getConstructor();
            return create.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("no valid constructor found", e);
        }
    }
}
