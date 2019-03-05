package de.dst.fortran.code.generator.kotlin

import de.dst.fortran.code.Type
import de.dst.fortran.code.TypeDef
import de.dst.fortran.code.TypeMap
import de.dst.fortran.code.Value
import de.irt.kfor.*
import kotlin.reflect.KClass

fun KClass<*>.isMutable(): Boolean {
    return String::class == this || this.javaPrimitiveType != null
}

fun typeOf(klass : KClass<*>) : Type = when(klass) {
        Boolean::class -> Type.L
        Byte::class -> Type.CH
        Char::class -> Type.CH
        Short::class -> Type.I2
        Int::class -> Type.I4
        Long::class -> Type.I4
        Float::class -> Type.R4
        Double::class -> Type.R8
        String::class -> Type.STR
        Cplx::class -> Type.CPX
        else -> Type.NONE
}

operator fun Type.times(that : Type) : Type = this.or(that)

class Types : TypeMap<KClass<*>>() {

    override fun get(definition: TypeDef?): KClass<*> {
        return if (definition == null || definition.type==Type.NONE)
            Unit::class
        else {
            val resolve = super.get(definition)

            if(resolve==null)
                throw NullPointerException("invalid type: $definition")

            return resolve
        }
    }

    init {

        kinds(Type.CH)
                .define(Value.Kind.INTRINSIC, Char::class)
                .define(Value.Kind.PRIMITIVE, Char::class)
                .define(Value.Kind.PROPERTY, Ch::class)
                .define(Value.Kind.ARRAY, Ch.Arr::class)
                .define(Value.Kind.MATRIX, Ch.Mat::class)

        kinds(Type.I2)
                .define(Value.Kind.INTRINSIC, Int::class)
                .define(Value.Kind.PRIMITIVE, Int::class)
                .define(Value.Kind.PROPERTY, IRef::class)
                .define(Value.Kind.ARRAY, IArr::class)
                .define(Value.Kind.MATRIX, IMat::class)
                .define(Value.Kind.CUBE, ICub::class)

        kinds(Type.I4)
                .define(Value.Kind.INTRINSIC, Int::class)
                .define(Value.Kind.PRIMITIVE, Int::class)
                .define(Value.Kind.PROPERTY, IRef::class)
                .define(Value.Kind.ARRAY, IArr::class)
                .define(Value.Kind.MATRIX, IMat::class)
                .define(Value.Kind.CUBE, ICub::class)

        kinds(Type.R4)
                .define(Value.Kind.INTRINSIC, Double::class)
                .define(Value.Kind.PRIMITIVE, Double::class)
                .define(Value.Kind.PROPERTY, Ref::class)
                .define(Value.Kind.ARRAY, RArr::class)
                .define(Value.Kind.MATRIX, RMat::class)
                .define(Value.Kind.CUBE, RCub::class)

        kinds(Type.R8)
                .define(Value.Kind.INTRINSIC, Double::class)
                .define(Value.Kind.PRIMITIVE, Double::class)
                .define(Value.Kind.PROPERTY, Ref::class)
                .define(Value.Kind.ARRAY, RArr::class)
                .define(Value.Kind.MATRIX, RMat::class)
                .define(Value.Kind.CUBE, RCub::class)

        kinds(Type.CPX)
                .define(Value.Kind.INTRINSIC, Cplx::class)
                .define(Value.Kind.PRIMITIVE, Cplx::class)
                .define(Value.Kind.PROPERTY, CRef::class)

        kinds(Type.L)
                .define(Value.Kind.INTRINSIC, Boolean::class)
                .define(Value.Kind.PRIMITIVE, Boolean::class)

        kinds(Type.STR)
                .define(Value.Kind.INTRINSIC, String::class)
                .define(Value.Kind.PRIMITIVE, String::class)
                .define(Value.Kind.PROPERTY, Str::class)
                .define(Value.Kind.ARRAY, Str.Arr::class)
    }

    fun MutableMap<Value.Kind, KClass<*>>.define(kind : Value.Kind, type : KClass<*>) : MutableMap<Value.Kind, KClass<*>> {
        put(kind, type)
        return this
    }
}