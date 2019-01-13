package de.irt.kfor

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  09.01.2019 09:27
 * modified by: $Author$
 * modified on: $Date$
 */
interface R8 {
    var v : Float

    companion object {
        operator fun invoke(value: Float = 0F): R8 {
            var v: Float = value
            return object : R8 {
                override var v = value
            }
        }
    }

    data class Arr (val len : Int) {
        val arr = DoubleArray(len)
        fun index(i : Int) = i-1
        operator fun get(i : Int) = arr[index(i)]
        operator fun set(i : Int, v : Double) {
            arr[index(i)] = v
        }
    }

    data class Mat (val ni : Int, val nj : Int) {
        val arr = DoubleArray(ni*nj)
        fun index(i : Int, j : Int) = i-1 + ni*(j-1)
        operator fun get(i : Int, j : Int) = arr[index(i, j)]
        operator fun set(i : Int, j : Int, v : Double) {
            arr[index(i, j)] = v
        }
    }

    data class Cub (val nx : Int, val ny : Int, val nz : Int) {
        val arr = DoubleArray(nx*ny*nz)
        fun index(i : Int, j : Int, k : Int) = i-1 + nx*((j) + ny*(k-1))
        operator fun get(i : Int, j : Int, k : Int) = arr[index(i,j,k)]
        operator fun set(i : Int, j : Int, k : Int, v : Double) {
            arr[index(i,j,k)] = v
        }
    }
}
