package de.dst.fortran.code;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.04.17
 * Time: 13:48
 */
public class Entities<T extends Entity> extends AbstractMap<String, T> {

    final Function<String, T> create;

    public final Map<String, T> entities = new HashMap<>();

    public Entities(Function<String, T> create) {
        this.create = create;
    }

    public T get(String name) {
        return entities.computeIfAbsent(name, create);
    }

    @Override
    public Set<Entry<String, T>> entrySet() {
        return entities.entrySet();
    }
}
