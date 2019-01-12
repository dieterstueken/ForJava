package de.dst.fortran.code.generator.java;

import de.dst.fortran.code.Type;
import de.dst.fortran.code.TypeDef;
import de.dst.fortran.code.TypeMap;
import de.dst.fortran.code.Value;
import de.irt.jfor.*;

import static de.dst.fortran.code.Value.Kind.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.01.19
 * Time: 15:42
 */
public class Types extends TypeMap<Class<?>> {

    public Class<?> get(TypeDef type) {
        if(type == null)
            return Void.TYPE;

        Class<?> resolve = super.get(type);
        if(resolve==null)
            throw new NullPointerException("invalid type: " + type);

        return resolve;
    }

    Types() {
        define(Type.STR, String.class, ChArr.class, StringArr.class);
        define(Type.CH, Byte.TYPE, I1.class, I1Arr.class, I1Mat.class);
        define(Type.I2, Short.TYPE, I2.class, I2Arr.class, I2Mat.class, I2Cub.class);
        define(Type.I4, Integer.TYPE, I4.class, I4Arr.class, I4Cub.class, I4Mat.class);
        define(Type.L4, Boolean.TYPE, L4.class);
        define(Type.R4, Float.TYPE, R4.class, R4Arr.class, R4Mat.class, R4Cub.class);
        define(Type.R8, Double.TYPE, R8.class, R8Arr.class, R8Mat.class, R8Cub.class);
        define(Type.CPX, Complex.class);
    }

    private void define(Type type, Class<?> ref) {
        put(type.kind(Value.Kind.PROPERTY), ref);
    }

    private void define(Type type, Class<?> primitive, Class<?> ref) {
        define(type, ref);
        put(type.kind(INTRINSIC), primitive);
        put(type.kind(PRIMITIVE), primitive);
    }

    private void define(Type type, Class<?> primitive, Class<?> ref, Class<?> arr) {
        define(type, primitive, ref);
        put(type.kind(ARRAY), arr);
    }

    private void define(Type type, Class<?> primitive, Class<?> ref, Class<?> arr, Class<?> mat) {
        define(type, primitive, ref, arr);
        put(type.kind(MATRIX), mat);
    }

    private void define(Type type, Class<?> primitive, Class<?> ref, Class<?> arr, Class<?> mat, Class<?> cub) {
        define(type, primitive, ref, arr, mat);
        put(type.kind(CUBE), cub);
    }
}
