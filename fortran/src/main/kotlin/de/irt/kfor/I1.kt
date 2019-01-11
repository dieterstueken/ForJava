package de.irt.kfor

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  09.01.2019 09:27
 * modified by: $Author$
 * modified on: $Date$
 */
interface I1 {
    var v : Short

    companion object {
        operator fun invoke(value: Short = 0): I1 {
            var v: Short = value
            return object : I1 {
                override var v = value
            }
        }
    }

    data class Arr (val len : Int) {
        val arr = ShortArray(len)
        fun index(i : Int) = i-1
        operator fun get(i : Int) = arr[index(i)]
        operator fun set(i : Int, v : Short) {
            arr[index(i)] = v
        }
    }

    data class Mat (val ni : Int, val nj : Int) {
        val arr = ShortArray(ni*nj)
        fun index(i : Int, j : Int) = i-1 + ni*(j-1)
        operator fun get(i : Int, j : Int) = arr[index(i, j)]
        operator fun set(i : Int, j : Int, v : Short) {
            arr[index(i, j)] = v
        }
    }

    data class Cub (val nx : Int, val ny : Int, val nz : Int) {
        val arr = ShortArray(nx*ny*nz)
        fun index(i : Int, j : Int, k : Int) = i-1 + nx*((j) + ny*(k-1))
        operator fun get(i : Int, j : Int, k : Int) = arr[index(i,j,k)]
        operator fun set(i : Int, j : Int, k : Int, v : Short) {
            arr[index(i,j,k)] = v
        }
    }
}
