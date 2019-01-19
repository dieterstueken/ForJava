package de.irt.kfor

import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.reflect.KClass

fun sqrt(value : Double) = Math.sqrt(value)
fun alog10(value : Double) = Math.log10(value)
fun abs(value : Double) = value.absoluteValue
fun abs(value : Int) = value.absoluteValue
fun toReal(value : Int) : Double = value.toDouble()
fun nint(value : Double) : Int = value.roundToInt()
fun sign(a : Double, b: Double) : Double {
    if(b<0)
        return -b.absoluteValue
    else
        return b.absoluteValue
}

infix fun Double.pow(p : Double) = Math.pow(this, p)

open class Fortran(val units : Units) {

    fun <U:Common> common(type : KClass<U>) : U = units.unit(type.java)

    fun <U:Fortran> function(type : KClass<U>) : U = units.unit(type.java)


}