package de.irt.kfor

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  18.01.2019 10:34
 * modified by: $Author$
 * modified on: $Date$
 */
interface Cub {
    val ni : Int
    val nj : Int
    val nk : Int

    val len : Int get() = ni*nj*nk

    fun index(i : Int, j : Int, k : Int) = i-1 + nj*((j) + nk*(k-1))
}