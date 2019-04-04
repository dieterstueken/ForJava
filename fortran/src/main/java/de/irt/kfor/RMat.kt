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

    operator fun invoke(i : Int) : RArr {
        return object : RArr {

            override fun allocate(ni: Int): RArr {
                return this
            }

            override val ni: Int
                get() = nj

            override fun set(j: Int, v: Double) {
                set(i, j, v)
            }

            override fun get(j: Int): Double {
                return get(i, j)
            }

        }
    }

    /**
     * matrix element by reference
     */
    operator fun invoke(i : Int, j : Int) = object : Ref {
        override var v : Double
            get() = this@RMat[i, j]
            set(v) = this@RMat.set(i,j,v)
    }

    fun i4(ni : Int, nj : Int) = R4.Mat(ni, nj)
    fun i8(ni : Int, nj : Int) = R8.Mat(ni, nj)
}