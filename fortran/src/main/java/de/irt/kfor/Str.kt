package de.irt.kfor

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  09.01.2019 09:27
 * modified by: $Author$
 * modified on: $Date$
 */
interface Str {
    var v : String

    companion object {
        operator fun invoke(value: String = ""): Str {
            var v: String = value
            return object : Str {
                override var v = value
            }
        }

        fun ref(v : String) = Str(v)
        fun arr(ni : Int) = I2.Arr(ni)
    }

    data class Arr (val len : Int) {
        val arr = mutableListOf<String>()
        fun index(i : Int) = i-1
        operator fun get(i : Int) = arr[index(i)]
        operator fun set(i : Int, v : String) {
            arr[index(i)] = v
        }

        constructor() : this(0)
    }
}
