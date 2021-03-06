package de.irt.kfor

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  18.01.2019 10:34
 * modified by: $Author$
 * modified on: $Date$
 */
interface RArr : Arr {
    operator fun get(i : Int) : Double
    operator fun set(i : Int, v : Double)
    fun allocate(ni : Int) : RArr

    fun assign(vararg values : Double) : Unit {
        for(i in 1 .. values.size) {
            set(i, values[i-1])
        }
    }

    fun assign(vararg values : Int) : Unit {
        for(i in 1 .. values.size) {
            set(i, values[i-1].toDouble())
        }
    }

    operator fun invoke(i : Int) : Ref {
        return object : Ref {
            override var v: Double
                get() = this@RArr.get(i)
                set(value) = this@RArr.set(i, value)
        }
    }

    companion object {
        fun r4(ni : Int) = R4.Arr(ni)
        fun r8(ni : Int) = R8.Arr(ni)
    }
}