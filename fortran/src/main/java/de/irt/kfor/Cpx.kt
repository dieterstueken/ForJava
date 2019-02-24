package de.irt.kfor

import de.irt.kfor.Fortran.Companion.sqrt

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.01.19
 * Time: 21:01
 */

data class Cpx(var re : Double, var im : Double) {

    constructor() : this(0.0, 0.0)

    companion object {
        fun toComplex(re : Double, im : Double) = Cpx(re,im)
    }

    fun assign (re : Double, im : Double) : Cpx {
        this.re = re
        this.im = im
        return this
    }

    infix fun assign (c : Cpx) : Cpx {
        re = c.re
        im = c.im
        return this
    }

    operator fun plus(c : Cpx) : Cpx {
        return Cpx(re+c.re, im+c.im)
    }

    operator fun minus(c : Cpx) : Cpx {
        return Cpx(re-c.re, im-c.im)
    }

    operator fun plus(d : Double) : Cpx {
        return Cpx(re+d, im)
    }

    operator fun minus(d : Double) : Cpx {
        return Cpx(re-d, im)
    }

    operator fun times(t : Double) : Cpx {
        return Cpx(re*t, im*t)
    }

    operator fun times(c : Cpx) : Cpx {
        return Cpx(re*c.re - im*c.im, im*c.re+re*c.im)
    }

    operator fun div(c : Cpx) : Cpx {
        val d = c.re*c.re + c.im*c.im
        return Cpx((re*c.re + im*c.im)/d, (im*c.re - re*c.im)/d)
    }

    fun csqrt() :Cpx {
        val z = cabs()
        return Cpx(sqrt(z+re), kotlin.math.sign(im)*sqrt(z-re))
    }

    fun cabs() : Double = Math.hypot(re, im)
}

