package de.irt.kfor

import kotlin.math.absoluteValue
import kotlin.reflect.KClass

open class Fortran(val units : Units) {

    fun <U:Common> common(type : KClass<U>) : U = units.unit(type.java)

    fun <U:Fortran> function(type : KClass<U>) : U = units.unit(type.java)

    fun sqrt(value : Double) = Math.sqrt(value)
    fun alog10(value : Double) = Math.log10(value)
    fun abs(value : Double) = value.absoluteValue
    fun abs(value : Int) = value.absoluteValue
    fun sign(a : Double, b: Double) : Double {
        if(b<0)
            return -b.absoluteValue
        else
            return b.absoluteValue
    }

    infix fun Double.pow(p : Double) = Math.pow(this, p)
}