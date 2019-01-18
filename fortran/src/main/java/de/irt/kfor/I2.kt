package de.irt.kfor

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  09.01.2019 09:27
 * modified by: $Author$
 * modified on: $Date$
 */
interface I2 {

    companion object {
        operator fun invoke(v : Int = 0) = IRef(v)

        fun arr(ni : Int) = Arr(ni)
        fun arr() = arr(0)
        fun mat(ni : Int,nj : Int) = Mat(ni, nj)
        fun mat() = mat(0,0)
        fun cub(ni : Int,nj : Int, nk : Int) = Cub(ni, nj, nk)
    }

    data class Arr (override val ni : Int) : IArr {
        val arr = ShortArray(len)
        override fun get(i : Int) = arr[index(i)].toInt()
        override fun set(i : Int, v : Int) {
            arr[index(i)] = v.toShort()
        }
        override fun allocate(ni : Int) = if(this.ni==ni) this else I4.Arr(ni)
    }

    data class Mat (override val ni : Int, override val nj : Int) : IMat {
        val arr = ShortArray(len)
        override fun get(i : Int, j : Int) = arr[index(i, j)].toInt()
        override fun set(i : Int, j : Int, v : Int) {
            arr[index(i, j)] = v.toShort()
        }
    }

    data class Cub (override val ni : Int, override val nj : Int, override val nk : Int) : ICub {
        val arr = ShortArray(len)
        override fun get(i : Int, j : Int, k : Int) = arr[index(i,j,k)].toInt()
        override fun set(i : Int, j : Int, k : Int, v : Int) {
            arr[index(i,j,k)] = v.toShort()
        }
    }
}
