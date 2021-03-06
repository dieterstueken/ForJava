package de.irt.kfor

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  18.01.2019 10:34
 * modified by: $Author$
 * modified on: $Date$
 */
interface IMat : Mat {
    operator fun get(i : Int, j : Int) : Int
    operator fun set(i : Int, j : Int, v : Int)
    fun allocate(ni : Int, nj : Int) : IMat

    operator fun invoke(i : Int) : IArr {
        return object : IArr {

            override fun allocate(ni: Int): IArr {
                return this
            }

            override val ni: Int
                get() = nj

            override fun set(j: Int, v: Int) {
                set(i, j, v)
            }

            override fun get(j: Int): Int {
                return get(i, j)
            }

        }
    }
    /**
     * matrix element by reference
     */
    operator fun invoke(i : Int, j : Int) = object : IRef {
        override var v : Int
            get() = this@IMat.get(i, j)
            set(v) = this@IMat.set(i,j,v)
    }

    companion object {
        fun i1(ni : Int, nj : Int) = I1.Mat(ni, nj)
        fun i2(ni : Int, nj : Int) = I2.Mat(ni, nj)
        fun i4(ni : Int, nj : Int) = I4.Mat(ni, nj)
    }
}