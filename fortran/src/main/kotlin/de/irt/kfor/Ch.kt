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
    }

    data class Arr (val len : Int) {
        val arr = CharArray(len)
        fun index(i : Int) = i-1
        operator fun get(i : Int) = arr[index(i)]
        operator fun set(i : Int, v : Char) {
            arr[index(i)] = v
        }
    }

    data class Mat (val ni : Int, val nj : Int) {
        val arr = CharArray(ni*nj)
        fun index(i : Int, j : Int) = i-1 + ni*(j-1)
        operator fun get(i : Int, j : Int) = arr[index(i, j)]
        operator fun set(i : Int, j : Int, v : Char) {
            arr[index(i, j)] = v
        }
    }

    data class Cub (val nx : Int, val ny : Int, val nz : Int) {
        val arr = CharArray(nx*ny*nz)
        fun index(i : Int, j : Int, k : Int) = i-1 + nx*((j) + ny*(k-1))
        operator fun get(i : Int, j : Int, k : Int) = arr[index(i,j,k)]
        operator fun set(i : Int, j : Int, k : Int, v : Char) {
            arr[index(i,j,k)] = v
        }
    }
}
