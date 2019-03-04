package de.irt.kfor

import de.irt.kfor.Fortran.Companion.sqrt

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.01.19
 * Time: 21:01
 */

data class Cplx(var re : Double, var im : Double) {

    constructor() : this(0.0, 0.0)

    operator fun plus(c : Cplx) : Cplx {
        return Cplx(re+c.re, im+c.im)
    }

    operator fun minus(c : Cplx) : Cplx {
        return Cplx(re-c.re, im-c.im)
    }

    operator fun plus(d : Double) : Cplx {
        return Cplx(re+d, im)
    }

    operator fun minus(d : Double) : Cplx {
        return Cplx(re-d, im)
    }

    operator fun times(t : Double) : Cplx {
        return Cplx(re*t, im*t)
    }

    operator fun times(c : Cplx) : Cplx {
        return Cplx(re*c.re - im*c.im, im*c.re+re*c.im)
    }

    operator fun div(c : Cplx) : Cplx {
        val d = c.re*c.re + c.im*c.im
        return Cplx((re*c.re + im*c.im)/d, (im*c.re - re*c.im)/d)
    }

    fun csqrt() :Cplx {
        val z = cabs()
        return Cplx(sqrt(z+re), kotlin.math.sign(im)*sqrt(z-re))
    }

    fun cabs() : Double = Math.hypot(re, im)
}

