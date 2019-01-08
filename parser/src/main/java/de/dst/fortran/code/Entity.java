package de.dst.fortran.code;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  07.02.13 18:49
 * modified by: $Author$
 * modified on: $Date$
 */
public class Entity implements Context {

    public final String name;

    public String getName() {
        return name;
    }

    public Entity(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entity)) return false;

        Entity entity = (Entity) o;

        return name.equals(entity.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
