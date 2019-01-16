package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import de.dst.fortran.code.TypeDef
import de.dst.fortran.code.Value
import de.dst.fortran.code.Variable
import kotlin.reflect.KClass

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.01.19
 * Time: 19:00
 */

fun Variable?.isProperty() = this?.type() == Value.Kind.PROPERTY

fun Variable.targetName() : String {
    var target : String = if(context!=null) ".${context.name}" else ""

    target += name

    if(isProperty())
        target += ".v"

    return target
}

abstract class CodeGenerator(val generators : CodeGenerators, val className : ClassName, val name : String) {

    abstract fun generate()

    abstract val initialize : String

    fun asProperty() : PropertySpec = PropertySpec.builder(name, className)
                .initializer(initialize, className)
                .build()

    fun TypeDef.asKlass() : KClass<*> = generators.types.get(this)

    fun Variable.asKlass() : KClass<*> = type().asKlass()

    fun Variable.asProperty() : PropertySpec {
        val klass = type().asKlass()
        return PropertySpec.builder(this.getName(), klass)
                .mutable(klass.isPrimitive())
                .initializer(this.initialize(klass))
                .build()
    }

    fun Variable.asParameter() = ParameterSpec.builder(this.getName(), type().asKlass()).build()

    fun Variable.initialize(type : KClass<*>) = when(type) {
        Byte::class, Short::class, Int::class, Long::class -> CodeBlock.of("0")
        Float::class -> CodeBlock.of("0.0F")
        Double::class -> CodeBlock.of("0.0")
        Char::class -> CodeBlock.of("' '")
        String::class -> CodeBlock.of("\"\"")
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