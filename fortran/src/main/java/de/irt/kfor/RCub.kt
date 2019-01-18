package de.irt.kfor

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  18.01.2019 10:34
 * modified by: $Author$
 * modified on: $Date$
 */
interface RCub : Cub {
    operator fun get(i : Int, j : Int, k : Int) : Double
    operator fun set(i : Int, j : Int, k : Int, v : Double)

    companion object {
        fun r4(ni : Int, nj : Int, nk : Int) = R4.Cub(ni, nj, nk)
        fun r8(ni : Int, nj : Int, nk : Int) = R8.Cub(ni, nj, nk)
    }
}