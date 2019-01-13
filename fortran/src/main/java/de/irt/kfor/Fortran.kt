package de.irt.kfor

import kotlin.reflect.KClass

class Fortran(val units : Units) {

    fun <U:Any> unit(type : KClass<U>) : U = units.unit(type.java)

}