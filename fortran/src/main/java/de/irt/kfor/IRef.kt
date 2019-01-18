package de.irt.kfor

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  18.01.2019 11:31
 * modified by: $Author$
 * modified on: $Date$
 */
interface IRef {
    var v : Int

    companion object {
        operator fun invoke(value : Int) = object : IRef {
            override var v = value
        }

        fun i1(value : Int = 0) = invoke(value)
        fun i2(value : Int = 0) = invoke(value)
        fun i4(value : Int = 0) = invoke(value)
    }
}