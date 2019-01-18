package de.irt.kfor

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  18.01.2019 10:34
 * modified by: $Author$
 * modified on: $Date$
 */
interface IArr : Arr {
    operator fun get(i : Int) : Int
    operator fun set(i : Int, v : Int)
    fun allocate(ni : Int) : IArr

    companion object {
        fun ch(ni : Int) = Ch.Arr(ni)
        fun i1(ni : Int) = I1.Arr(ni)
        fun i2(ni : Int) = I2.Arr(ni)
        fun i4(ni : Int) = I4.Arr(ni)
    }
}