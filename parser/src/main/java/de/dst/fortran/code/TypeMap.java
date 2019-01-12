package de.dst.fortran.code;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.01.19
 * Time: 18:21
 */
public class TypeMap<T> {

    public T get(TypeDef type) {
        return kinds(type.getType()).get(type.getKind());
    }

    private final Map<Type, Map<Value.Kind, T>> types = new EnumMap<>(Type.class);

    protected Map<Value.Kind, T> kinds(Type type) {
        return types.computeIfAbsent(type, key -> new EnumMap<>(Value.Kind.class));
    }

    protected void put(TypeDef type, T value) {
        kinds(type.getType()).put(type.getKind(), value);
    }
}
