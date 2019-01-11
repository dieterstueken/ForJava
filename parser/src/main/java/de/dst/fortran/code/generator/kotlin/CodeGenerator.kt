package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.ClassName
import de.irt.jfor.Common
import java.io.File

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  10.01.2019 09:47
 * modified by: $Author$
 * modified on: $Date$
 */

class CodeGenerator(val root : File) {

    fun generateCommon(pkg: String, common : Common) {
        val commonClass = ClassName("", "Greeter")
    }



}