package de.dst.fortran.code;

import java.util.EnumMap;

import static de.dst.fortran.code.Value.Kind.*;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  07.02.13 18:55
 * modified by: $Author$
 * modified on: $Date$
 */
public enum Type {

    NONE, L, CH, I2, I4, R4, R8, CPX, STR;

    public static Type parse(String token) {
        if(token==null || token.isEmpty())
            return NONE;

        switch(token) {
            case "character*1": return CH;
            case "integer": return I2;
            case "integer*2": return I2;
            case "integer*4": return I4;
            case "logical": return L;
            case "logical*1": return L;
            case "logical*2": return L;
            case "logical*4": return L;
            case "real": return R4;
            case "real*4": return R4;
            case "real*8": return R8;
            case "complex": return CPX;
        }

        // fallback
        if(token.startsWith("character*")) {
            return STR; // whatever
        }

        throw new IllegalArgumentException(token);
    }

    // parse or fallback to intrinsic
    public static Type parse(String token, String name) {
        if(token==null || token.isEmpty())
            return intrinsic(name);
        else
            return Type.parse(token);
    }

    public static Type intrinsic(String name) {
        char c = Character.toLowerCase(name.charAt(0));

        if("ijklmn".indexOf(c)>=0)
            return I4;

        return R8;
    }

    public Type or(Type that) {
        return this.ordinal() > that.ordinal() ? this : that;
    }

    public TypeDef kind(Value.Kind kind) {
        return kinds.get(kind);
    }

    public boolean isInt() {
        return this==I2 || this==I4;
    }

    public boolean isReal() {
        return this==R4 || this==R8;
    }

    public TypeDef primitive() {
        return kind(Value.Kind.PRIMITIVE);
    }

    public TypeDef dim(int n) {
        switch(n) {
            case 0: return kind(PROPERTY);
            case 1: return kind(ARRAY);
            case 2: return kind(MATRIX);
            case 3: return kind(CUBE);
        }

        throw new IllegalArgumentException(Integer.toString(n));

    }

    private EnumMap<Value.Kind, TypeDef> kinds = new EnumMap<>(Value.Kind.class);
    {
        for (Value.Kind kind : Value.Kind.values()) {
            kinds.put(kind, TypeDef.of(this, kind));
        }
    }

}
