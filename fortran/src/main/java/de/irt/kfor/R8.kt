package de.irt.kfor

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  09.01.2019 09:27
 * modified by: $Author$
 * modified on: $Date$
 */
interface R8 {

    companion object {
        operator fun invoke(v : Double = 0.0) = Ref(v)
        fun arr(ni : Int) = Arr(ni)
        fun mat(ni : Int,nj : Int) = I4.Mat(ni, nj)
        fun cub(ni : Int,nj : Int, nk : Int) = I4.Cub(ni, nj, nk)
    }

    data class Arr (override val ni : Int) : RArr {
        val arr = DoubleArray(len)
        override fun get(i : Int) = arr[index(i)]
        override fun set(i : Int, v : Double) {
            arr[index(i)] = v
        }
        override fun allocate(ni : Int) = if(this.len==ni) this else R4.Arr(ni)
    }

    data class Mat (override val ni : Int, override val nj : Int) : RMat {
        val arr = DoubleArray(len)
        override fun get(i : Int, j : Int) = arr[index(i, j)]
        override fun set(i : Int, j : Int, v : Double) {
            arr[index(i, j)] = v
        }
    }

    data class Cub (override val ni : Int, override val nj : Int, override val nk : Int) : RCub {
        val arr = DoubleArray(len)
        override fun get(i : Int, j : Int, k : Int) = arr[index(i,j,k)]
        override fun set(i : Int, j : Int, k : Int, v : Double) {
            arr[index(i,j,k)] = v
        }
    }
}
