package de.dst.fortran.code;


import de.irt.jfor.*;

import java.util.Arrays;
import java.util.List;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  07.02.13 18:55
 * modified by: $Author$
 * modified on: $Date$
 */
public class Type {

    public final Class<?> simple;

    public final List<Class<?>> types;

    public Type(Class<?> simple, Class<?> ... types) {
        this.simple = simple;
        this.types = Arrays.asList(types);
    }

    static protected Type of(Class<?> simple, Class<?> ... types) {
        return new Type(simple, types);
    }

    public Class<?> type() {
        return types.get(0);
    }

    public Class<?> type(int dim) {
        return types.get(dim);
    }

    public String toString() {
        return type().getSimpleName();
    }

    public static final Type STR= of(String.class, ChArr.class, StringArr.class);
    public static final Type CH = of(Byte.TYPE, I1.class, I1Arr.class, I1Mat.class);
    public static final Type I2 = of(Short.TYPE, I2.class, I2Arr.class, I2Mat.class, I2Cub.class);
    public static final Type I4 = of(Integer.TYPE, I4.class, I4Cub.class, I4Arr.class, I4Mat.class);
    public static final Type L4 = of(Boolean.TYPE, L4.class);
    public static final Type R4 = of(Float.TYPE, R4.class, R4Arr.class, R4Mat.class, R4Cub.class);
    public static final Type R8 = of(Double.TYPE, R8.class, R8Arr.class, R8Mat.class, R8Cub.class);
    public static final Type CX = of(null, Complex.class);

    public static Type intrinsic(String name) {
        return "ijklmn".indexOf(Character.toLowerCase(name.charAt(0)))>=0 ? I4 : R4;
    }
}
