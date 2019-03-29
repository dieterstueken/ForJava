package de.irt.kfor

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  09.01.2019 09:27
 * modified by: $Author$
 * modified on: $Date$
 */
interface Ch {
    var v : Char

    companion object {
        operator fun invoke(value: Char = ' '): Ch {
            var v: Char = value
            return object : Ch {
                override var v = value
            }
        }

        fun ref(i : Int) = IRef(i)
        fun arr(ni : Int) = Arr(ni)
        fun mat() = mat(0,0)
        fun mat(ni : Int,nj : Int) = Mat(ni, nj)
        fun cub(ni : Int,nj : Int, nk : Int) = Cub(ni, nj, nk)
    }

    data class Arr (override val ni : Int) : de.irt.kfor.Arr {
        val arr = CharArray(len)
        operator fun get(i : Int) = arr[index(i)]
        operator fun set(i : Int, v : Char) {
            arr[index(i)] = v
        }

        constructor() : this(0)

        fun allocate(ni : Int) = if(this.len==ni) this else Arr(ni)
    }

    data class Mat (override val ni : Int, override val nj : Int) : de.irt.kfor.Mat {
        val arr = CharArray(ni*nj)
        operator fun get(i : Int, j : Int) = arr[index(i, j)]
        operator fun set(i : Int, j : Int, v : Char) {
            arr[index(i, j)] = v
        }

        fun allocate(ni : Int, nj : Int) = if(this.ni==ni&&this.nj==nj) this else Mat(ni, nj)
    }

    data class Cub (override val ni : Int, override val nj : Int, override val nk : Int) : de.irt.kfor.Cub {
        val arr = CharArray(ni*nj*nk)
        operator fun get(i : Int, j : Int, k : Int) = arr[index(i,j,k)]
        operator fun set(i : Int, j : Int, k : Int, v : Char) {
            arr[index(i,j,k)] = v
        }
    }
}
