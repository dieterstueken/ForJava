package de.irt.kfor

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.01.19
 * Time: 21:01
 */
interface Cpx {

    var re : Float

    var im : Float

    companion object {
        operator fun invoke(re : Float, im : Float) : Cpx {
            return object : Cpx {
                override var re: Float = re
                override var im: Float = im
            }
        }
    }
}