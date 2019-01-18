package de.irt.kfor

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  18.01.2019 10:34
 * modified by: $Author$
 * modified on: $Date$
 */
interface RMat : Mat {
    operator fun get(i : Int, j : Int) : Double
    operator fun set(i : Int, j : Int, v : Double)

    fun i4(ni : Int, nj : Int) = R4.Mat(ni, nj)
    fun i8(ni : Int, nj : Int) = R8.Mat(ni, nj)
}