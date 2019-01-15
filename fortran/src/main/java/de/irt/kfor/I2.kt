package de.irt.kfor

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  09.01.2019 09:27
 * modified by: $Author$
 * modified on: $Date$
 */
interface I2 {
    var v : Byte

    companion object {
        operator fun invoke(value: Byte = 0): I2 {
            var v: Byte = value
            return object : I2 {
                override var v = value
            }
        }
    }

    data class Arr (val len : Int) {
        val arr = ByteArray(len)
        fun index(i : Int) = i-1
        operator fun get(i : Int) = arr[index(i)].toInt()
        operator fun set(i : Int, v : Int) {
            arr[index(i)] = v.toByte()
        }
    }

    data class Mat (val ni : Int, val nj : Int) {
        val arr = ByteArray(ni*nj)
        fun index(i : Int, j : Int) = i-1 + ni*(j-1)
        operator fun get(i : Int, j : Int) = arr[index(i, j)].toInt()
        operator fun set(i : Int, j : Int, v : Integer) {
            arr[index(i, j)] = v.toByte()
        }
    }

    data class Cub (val nx : Int, val ny : Int, val nz : Int) {
        val arr = ByteArray(nx*ny*nz)
        fun index(i : Int, j : Int, k : Int) = i-1 + nx*((j) + ny*(k-1))
        operator fun get(i : Int, j : Int, k : Int) = arr[index(i,j,k)].toInt()
        operator fun set(i : Int, j : Int, k : Int, v : Integer) {
            arr[index(i,j,k)] = v.toByte()
        }
    }
}
