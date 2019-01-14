package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.PropertySpec
import de.dst.fortran.code.Context
import de.dst.fortran.code.Variable
import kotlin.reflect.KClass

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.01.19
 * Time: 19:00
 */

abstract class CodeGenerator<T : Context>(val generators : CodeGenerators, val className : ClassName, val block : T) {

    abstract fun generate()

    abstract val initialize : String

    fun asProperty() = PropertySpec.builder(block.name, className)
                .initializer(initialize, className)
                .build()

    fun Variable.asProperty() : PropertySpec {
        val type = generators.getKlass(this)
        return PropertySpec.builder(this.getName(), type)
                .mutable(type.javaPrimitiveType!=null)
                .initializer(this.initialize(type))
                .build()
    }

    fun Variable.initialize(type : KClass<*>) = when(type) {
        Byte::class, Short::class, Int::class, Long::class -> CodeBlock.of("0")
        Float::class -> CodeBlock.of("0.0F")
        Double::class -> CodeBlock.of("0.0")
        String::class -> CodeBlock.of("")
        Boolean::class -> CodeBlock.of("false")
        else -> {
            when(this.dim.size) {
                1 -> CodeBlock.of("%T(%L)", type, this.dim[0])
                2 -> CodeBlock.of("%T(%L, %L)", type, this.dim[0], this.dim[1])
                3 -> CodeBlock.of("%T(%L, %L, %L)", type, this.dim[0], this.dim[1], this.dim[2])
                else -> CodeBlock.of("%T()", type)
            }
        }
    }
}