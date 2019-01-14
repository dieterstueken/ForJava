package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.ClassName

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.01.19
 * Time: 19:00
 */

abstract class CodeGenerator(val generators : CodeGenerators, val className : ClassName) {
    abstract fun build()
}