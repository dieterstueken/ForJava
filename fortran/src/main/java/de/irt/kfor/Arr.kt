package de.irt.kfor

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  18.01.2019 10:34
 * modified by: $Author$
 * modified on: $Date$
 */
interface Arr {
    val ni : Int

    val len : Int get() = ni

    fun index(i : Int) = i-1
}