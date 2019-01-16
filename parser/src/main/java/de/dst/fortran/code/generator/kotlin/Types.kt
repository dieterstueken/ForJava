package de.dst.fortran.code.generator.kotlin

import de.dst.fortran.code.Type
import de.dst.fortran.code.TypeDef
import de.dst.fortran.code.TypeMap
import de.dst.fortran.code.Value
import de.irt.kfor.*
import kotlin.reflect.KClass

fun KClass<*>.isPrimitive(): Boolean {
    return String::class == this || this.javaPrimitiveType != null
}

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.01.19
 * Time: 18:01
 */
class Types : TypeMap<KClass<*>>() {

    override fun get(type: TypeDef?): KClass<*> {
        return if (type == null)
            Unit::class
        else {
            val resolve = super.get(type)

            if(resolve==null)
                throw NullPointerException("invalid type: $type")

            return resolve
        }
    }

    init {

        kinds(Type.STR)
                .define(Value.Kind.INTRINSIC, String::class)
                .define(Value.Kind.PRIMITIVE, String::class)
                .define(Value.Kind.PROPERTY, Str::class)
                .define(Value.Kind.ARRAY, Str.Arr::class)

        kinds(Type.CH)
                .define(Value.Kind.INTRINSIC, Char::class)
                .define(Value.Kind.PRIMITIVE, Char::class)
                .define(Value.Kind.PROPERTY, Ch::class)
                .define(Value.Kind.ARRAY, Ch.Arr::class)
                .define(Value.Kind.MATRIX, Ch.Mat::class)
                .define(Value.Kind.CUBE, Ch.Cub::class)

        kinds(Type.I2)
                .define(Value.Kind.INTRINSIC, Int::class)
                .define(Value.Kind.PRIMITIVE, Int::class)
                .define(Value.Kind.PROPERTY, I2::class)
                .define(Value.Kind.ARRAY, I2.Arr::class)
                .define(Value.Kind.MATRIX, I2.Mat::class)
                .define(Value.Kind.CUBE, I2.Cub::class)

        kinds(Type.I4)
                .define(Value.Kind.INTRINSIC, Int::class)
                .define(Value.Kind.PRIMITIVE, Int::class)
                .define(Value.Kind.PROPERTY, I4::class)
                .define(Value.Kind.ARRAY, I4.Arr::class)
                .define(Value.Kind.MATRIX, I4.Mat::class)
                .define(Value.Kind.CUBE, I4.Cub::class)

        kinds(Type.R4)
                .define(Value.Kind.INTRINSIC, Double::class)
                .define(Value.Kind.PRIMITIVE, Double::class)
                .define(Value.Kind.PROPERTY, R4::class)
                .define(Value.Kind.ARRAY, R4.Arr::class)
                .define(Value.Kind.MATRIX, R4.Mat::class)
                .define(Value.Kind.CUBE, R4.Cub::class)

        kinds(Type.R8)
                .define(Value.Kind.INTRINSIC, Double::class)
                .define(Value.Kind.PRIMITIVE, Double::class)
                .define(Value.Kind.PROPERTY, R8::class)
                .define(Value.Kind.ARRAY, R8.Arr::class)
                .define(Value.Kind.MATRIX, R8.Mat::class)
                .define(Value.Kind.CUBE, R8.Cub::class)

        kinds(Type.L4)
                .define(Value.Kind.INTRINSIC, Boolean::class)
                .define(Value.Kind.PRIMITIVE, Boolean::class)

        kinds(Type.CPX)
                .define(Value.Kind.PROPERTY, Cpx::class)
    }

    fun MutableMap<Value.Kind, KClass<*>>.define(kind : Value.Kind, type : KClass<*>) : MutableMap<Value.Kind, KClass<*>> {
        put(kind, type)
        return this
    }

}