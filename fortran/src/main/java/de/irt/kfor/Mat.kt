package de.irt.kfor

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  18.01.2019 10:34
 * modified by: $Author$
 * modified on: $Date$
 */
interface Mat {
    val ni : Int
    val nj : Int
    val len : Int get() = ni*nj

    fun index(i : Int, j : Int) = i-1 + ni*(j-1)
}