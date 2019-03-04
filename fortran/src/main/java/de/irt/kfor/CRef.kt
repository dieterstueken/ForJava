package de.irt.kfor

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  18.01.2019 11:31
 * modified by: $Author$
 * modified on: $Date$
 */
interface CRef {
    var v : Cplx

    companion object {
        operator fun invoke(value : Cplx) = object : CRef {
            override var v = value
        }

        operator fun invoke() = invoke(Cplx())
    }
}