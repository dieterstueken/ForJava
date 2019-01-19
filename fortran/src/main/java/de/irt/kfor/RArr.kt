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

    companion object {
        fun r4(ni : Int) = R4.Arr(ni)
        fun r8(ni : Int) = R8.Arr(ni)
    }
}