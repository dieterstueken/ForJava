package de.irt.kfor

import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.reflect.KClass


open class Fortran(val units : Units) {

    fun <U:Common> common(type : KClass<U>) : U = units.unit(type.java)

    fun <U:Fortran> function(type : KClass<U>) : U = units.unit(type.java)

    companion object {
        fun alog10(value : Double) = kotlin.math.log10(value)
        fun alog(value : Double) = kotlin.math.ln(value)
        fun sqrt(value : Double) = kotlin.math.sqrt(value)
        fun dsqrt(value : Double) = kotlin.math.sqrt(value)

        fun csqrt(v : Cpx) : Cpx = v.csqrt()
        operator fun Double.plus(v : Cpx) = v+this
        operator fun Double.minus(v : Cpx) = Cpx(this-v.re, v.im)

        fun abs(value : Double) = value.absoluteValue
        fun abs(value : Int) = value.absoluteValue
        fun iabs(value : Int) = value.absoluteValue
        fun cabs(value : Cpx) = value.cabs()

        fun toInt(value : Double) : Int = value.toInt()
        fun idint(value : Double) : Int = value.toInt()
        fun ifix(value : Double) : Int = value.toInt()
        fun toReal(value : Int) : Double = value.toDouble()
        fun dble(value : Int) : Double = value.toDouble()
        fun dble(value : Double) : Double = value.toDouble()

        fun nint(value : Double) : Int = value.roundToInt()
        fun sign(a : Double, b: Double) : Double {
            if(b<0)
                return -b.absoluteValue
            else
                return b.absoluteValue
        }

        fun min(a : Double, b: Double) : Double = kotlin.math.min(a,b)
        fun min0(a : Int, b: Int) : Int = kotlin.math.min(a,b)
        fun amin0(a : Int, b: Int) : Int = kotlin.math.min(a,b)
        fun amin1(a : Double, b: Double) : Double = kotlin.math.min(a,b)

        fun max(a : Double, b: Double) : Double = kotlin.math.max(a,b)
        fun max(a : Double, b: Double, c:Double) : Double = kotlin.math.max(kotlin.math.max(a,b), c)
        fun max0(a : Int, b: Int) : Int = kotlin.math.max(a,b)
        fun amax0(a : Int, b: Int) : Int = kotlin.math.max(a,b)
        fun amax1(a : Double, b: Double) : Double = kotlin.math.max(a,b)

        fun len_trim(s : Str) : Int = s.v.trim().length
        
        fun sin(a : Double) : Double = Math.sin(a)
        fun cos(a : Double) : Double = Math.cos(a)
        fun atan2(a : Double, b: Double) : Double = Math.atan2(a,b)
        fun datan2(a : Double, b: Double) : Double = Math.atan2(a,b)
        fun atan(a : Double) : Double = Math.atan(a)
        fun tanh(a : Double) : Double = Math.tanh(a)
        fun exp(a : Double) : Double = Math.exp(a)

        fun pow(a : Double, b: Int) : Double = Math.pow(a, b.toDouble())
        fun pow(a : Double, b: Double) : Double = Math.pow(a, b)
    }
}