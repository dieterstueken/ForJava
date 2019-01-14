package de.irt.kfor

import kotlin.reflect.KClass

open class Fortran(val units : Units) {

    fun <U:Common> common(type : KClass<U>) : U = units.unit(type.java)

    fun <U:Fortran> function(type : KClass<U>) : U = units.unit(type.java)

}