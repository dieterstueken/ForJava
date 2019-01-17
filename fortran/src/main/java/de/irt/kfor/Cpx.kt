package de.irt.kfor

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.01.19
 * Time: 21:01
 */
interface Cpx {

    var re : Double

    var im : Double

    companion object {

        operator fun invoke() : Cpx {
            return invoke(0.0, 0.0)
        }

        operator fun invoke(re : Double, im : Double) : Cpx {
            return object : Cpx {
                override var re: Double = re
                override var im: Double = im
            }
        }
    }
}