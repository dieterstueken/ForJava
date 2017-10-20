package de.dst.fortran.code;


import de.irt.jfor.*;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  07.02.13 18:55
 * modified by: $Author$
 * modified on: $Date$
 */
public class Type {

    public final Class<?> type;

    public final Type arr;

    public Type(Class<?> type, Type arr) {
        this.type = type;
        this.arr = arr;
    }

    static protected Type of(Class<?> type) {
        return new Type(type, null);
    }
    
    protected Type on(Class<?> type) {
        return new Type(type, this);   
    }

    public Class<?> type() {
        return type;
    }

    public String toString() {
        return type.getSimpleName();
    }

    public static final Type CH = of(StringArr.class).on(ChArr.class).on(Ch.class);
    public static final Type I2 = of(I2Cub.class).on(I2Mat.class).on(I2Arr.class).on(I2.class);
    public static final Type I4 = of(I4Cub.class).on(I4Mat.class).on(I4Arr.class).on(I4.class);
    public static final Type L4 = of(L4.class);
    public static final Type R4 = of(R4Cub.class).on(R4Mat.class).on(R4Arr.class).on(R4.class);
    public static final Type R8 = of(R8Cub.class).on(R8Mat.class).on(R8Arr.class).on(R8.class);
    public static final Type CX = of(Complex.class);

    public static Type intrinsic(String name) {
        return "ijklmn".indexOf(Character.toLowerCase(name.charAt(0)))>=0 ? I4 : R4;
    }
}
