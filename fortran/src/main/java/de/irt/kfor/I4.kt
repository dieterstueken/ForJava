package de.irt.kfor

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  09.01.2019 09:27
 * modified by: $Author$
 * modified on: $Date$
 */
interface I4 {

    companion object {
        operator fun invoke(v : Int = 0) = IRef(v)
        fun arr(ni : Int) = Arr(ni)
        fun mat(ni : Int,nj : Int) = Mat(ni, nj)
        fun cub(ni : Int,nj : Int, nk : Int) = Cub(ni, nj, nk)
    }

    data class Arr(override val ni : Int) : IArr {
        val arr = IntArray(len)
        override operator fun get(i : Int) = arr[index(i)]
        override operator fun set(i : Int, v : Int) {
            arr[index(i)] = v
        }
        override fun allocate(ni : Int) = if(this.ni==ni) this else Arr(ni)
    }

    class Mat(override val ni : Int, override val nj : Int) : IMat {
        val arr = IntArray(len)
        override operator fun get(i : Int, j : Int) : Int = arr[index(i,j)]
        override operator fun set(i : Int, j : Int, v : Int) {
            arr[index(i,j)] = v
        }
        override fun allocate(ni : Int, nj : Int) = if(this.ni==ni&&this.nj==nj) this else Mat(ni, nj)
    }

    class Cub(override val ni : Int, override val nj : Int,  override val nk : Int) : ICub {
        val arr = IntArray(len)
        override operator fun get(i : Int, j : Int, k : Int) : Int = arr[index(i,j,k)]
        override operator fun set(i : Int, j : Int, k : Int, v : Int) {
            arr[index(i,j,k)] = v
        }
    }
}