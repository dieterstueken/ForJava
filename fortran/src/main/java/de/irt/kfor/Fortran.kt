package de.irt.kfor

import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.reflect.KClass


open class Fortran(val units : Units) {

    fun <U:Common> common(type : KClass<U>) : U = units.unit(type.java)

    fun <U:Fortran> function(type : KClass<U>) : U = units.unit(type.java)

    companion object {
        fun alog10(value : Double) = kotlin.math.log10(value)
        fun sqrt(value : Double) = kotlin.math.sqrt(value)

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

        fun min(a : Double, b: Double) : Double = kotlin.math.min(a,b)
        fun min0(a : Int, b: Int) : Int = kotlin.math.min(a,b)
        fun amin0(a : Int, b: Int) : Int = kotlin.math.min(a,b)
        fun amin1(a : Double, b: Double) : Double = kotlin.math.min(a,b)

        fun max(a : Double, b: Double) : Double = kotlin.math.max(a,b)
        fun max(a : Double, b: Double, c:Double) : Double = kotlin.math.max(kotlin.math.max(a,b), c)
        fun amax1(a : Double, b: Double) : Double = kotlin.math.max(a,b)

        fun tanh(a : Double) : Double = Math.tanh(a)
        fun exp(a : Double) : Double = Math.exp(a)

        fun Double.pow(b: Int) : Double = Math.pow(this, b.toDouble())
        fun Double.pow(b: Double) : Double = Math.pow(this, b)
    }
}