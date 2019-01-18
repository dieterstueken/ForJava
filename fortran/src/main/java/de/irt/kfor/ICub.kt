package de.irt.kfor

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  18.01.2019 10:34
 * modified by: $Author$
 * modified on: $Date$
 */
interface ICub : Cub {
    operator fun get(i : Int, j : Int, k : Int) : Int
    operator fun set(i : Int, j : Int, k : Int, v : Int)

    companion object {
        fun i1(ni : Int, nj : Int, nk : Int) = I1.Cub(ni, nj, nk)
        fun i2(ni : Int, nj : Int, nk : Int) = I2.Cub(ni, nj, nk)
        fun i4(ni : Int, nj : Int, nk : Int) = I4.Cub(ni, nj, nk)
    }
}