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

        fun csqrt(v : Cplx) : Cplx = v.csqrt()
        operator fun Double.plus(v : Cplx) = v+this
        operator fun Double.minus(v : Cplx) = Cplx(this-v.re, v.im)

        fun iabs(value : Int) : Int = value.absoluteValue
        fun abs(value : Double) = value.absoluteValue
        fun abs(value : Int) : Double = value.absoluteValue.toDouble()
        fun cabs(value : Cplx) = value.cabs()

        fun idint(value : Double) : Int = value.toInt()
        fun ifix(value : Double) : Int = value.toInt()

        fun intg(value : Double) : Int = value.toInt()
        fun real(value : Int) : Double = value.toDouble()
        fun dble(value : Int) : Double = value.toDouble()
        fun dble(value : Double) : Double = value.toDouble()
        fun cplx(re : Double, im : Double) = Cplx(re, im)

        fun nint(value : Double) : Int = value.roundToInt()
        fun sign(a : Double, b: Double) : Double {
            if(b<0)
                return -b.absoluteValue
            else
                return b.absoluteValue
        }

        fun min(a : Double, b: Double) : Double = kotlin.math.min(a,b)
        fun min0(a : Int, b: Int) : Int = kotlin.math.min(a,b)
        fun amin0(a : Int, b: Int) : Double = kotlin.math.min(a,b).toDouble()
        fun amin1(a : Double, b: Double) : Double = kotlin.math.min(a,b)

        fun max(a : Double, b: Double) : Double = kotlin.math.max(a,b)
        fun max(a : Double, b: Double, c:Double) : Double = kotlin.math.max(kotlin.math.max(a,b), c)
        fun max0(a : Int, b: Int) : Int = kotlin.math.max(a,b)
        fun amax0(a : Int, b: Int) : Double = kotlin.math.max(a,b).toDouble()
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