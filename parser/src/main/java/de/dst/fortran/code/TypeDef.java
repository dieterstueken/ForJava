package de.dst.fortran.code;

import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.01.19
 * Time: 14:30
 */
public class TypeDef {

    private final Type type;

    private final Value.Kind kind;

    private TypeDef(Type type, Value.Kind kind) {
        this.type = type;
        this.kind = kind;
    }

    Type getType() {
        return type;
    }

    Value.Kind getKind() {
        return kind;
    }

    static TypeDef of(Type type, Value.Kind kind) {
        return new TypeDef(type, kind);
    }

    @Override
    public String toString() {
        return getType() + "." + getKind();
    }

    @Override
    public int hashCode() {
        return 7 * getType().ordinal() + getKind().ordinal();
    }

    private boolean _equals(TypeDef def) {
        return Objects.equals(getType(), def.getType())
                && Objects.equals(getKind(), def.getKind());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TypeDef && _equals((TypeDef) obj);
    }
}
