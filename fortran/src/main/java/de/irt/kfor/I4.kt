package de.irt.kfor

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  09.01.2019 09:27
 * modified by: $Author$
 * modified on: $Date$
 */
interface I4 {
    var v : Int

    companion object {
        operator fun invoke(value: Int = 0): I4 {
            var v: Int = value
            return object : I4 {
                override var v = value
            }
        }
    }

    data class Arr (val len : Int) {
        val arr = IntArray(len)
        fun index(i : Int) = i-1
        operator fun get(i : Int) = arr[index(i)]
        operator fun set(i : Int, value : Int) {
            arr[index(i)] = value
        }

        operator fun invoke(i: Int): I4 {
            return object : I4 {
                override var v: Int
                    get() = this@Arr.get(i)
                    set(value) {this@Arr.set(i, value)}
            }
        }
    }

    data class Mat (val ni : Int, val nj : Int) {
        val arr = IntArray(ni*nj)
        fun index(i : Int, j : Int) = i-1 + ni*(j-1)
        operator fun get(i : Int, j : Int) = arr[index(i, j)]
        operator fun set(i : Int, j : Int, v : Int) {
            arr[index(i, j)] = v
        }

        operator fun invoke(i: Int, j : Int): I4 {
            return object : I4 {
                override var v: Int
                    get() = this@Mat.get(i, j)
                    set(value) {this@Mat.set(i, j, value)}
            }
        }
    }

    data class Cub (val nx : Int, val ny : Int, val nz : Int) {
        val arr = IntArray(nx*ny*nz)
        fun index(i : Int, j : Int, k : Int) = i-1 + nx*((j) + ny*(k-1))
        operator fun get(i : Int, j : Int, k : Int) = arr[index(i,j,k)]
        operator fun set(i : Int, j : Int, k : Int, v : Int) {
            arr[index(i,j,k)] = v
        }

        operator fun invoke(i : Int, j : Int, k : Int): I4 {
            return object : I4 {
                override var v: Int
                    get() = this@Cub.get(i, j, k)
                    set(value) {this@Cub.set(i, j, k, value)}
            }
        }
    }
}
