package de.irt.kfor

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  18.01.2019 11:31
 * modified by: $Author$
 * modified on: $Date$
 */
interface Ref {
    var v : Double

    fun assign(value : Double) : Unit {
        v = value;
    }

    companion object {
        operator fun invoke(value : Double) = object : Ref {
            override var v = value
        }
        fun r4(value : Double = 0.0) = invoke(value)
        fun r8(value : Double = 0.0) = invoke(value)
    }
}