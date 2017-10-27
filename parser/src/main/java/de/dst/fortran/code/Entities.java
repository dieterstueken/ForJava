package de.dst.fortran.code;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.04.17
 * Time: 13:48
 */
public class Entities<T extends Entity> extends AbstractSet<T> {

    final Function<String, T> create;

    public final Map<String, T> entities = new LinkedHashMap<>();

    public Entities(Function<String, T> create) {
        this.create = create;
    }

    public T get(String name) {
        return entities.computeIfAbsent(name, create);
    }

    // lookup, don't create
    public T find(String name) {
        return entities.get(name);
    }

    public boolean exists(String name) {
        return entities.containsKey(name);
    }

    @Override
    public Iterator<T> iterator() {
        return entities.values().iterator();
    }

    public T get(int index) {
        Iterator<T> it = iterator();

        while(index>0) {
            it.next();
            --index;
        }

        return it.next();
    }

    @Override
    public int size() {
        return entities.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entities)) return false;
        if (!super.equals(o)) return false;

        Entities<?> entities1 = (Entities<?>) o;

        return entities.equals(entities1.entities);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + entities.hashCode();
        return result;
    }
}
