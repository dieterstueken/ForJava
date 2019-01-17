package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.*
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

abstract class ClassGenerator(val generators : CodeGenerators, val className : ClassName, val property : PropertySpec) {

    constructor(generators : CodeGenerators, type : String, className : ClassName)
            : this(generators, className, PropertySpec.builder(className.simpleName.toLowerCase(), className)
                                        .initializer("$type(%T::class)", className)
                                        .build())

    fun getKlass(type : TypeDef?) : KClass<*> = generators.types.get(type)

    fun Variable.asProperty() : PropertySpec {
        val klass = getKlass(type())
        return PropertySpec.builder(name, klass)
                .mutable(klass.isPrimitive())
                .initializer(this.initialize(klass))
                .build()
    }

    open fun generate() {

        val spec = TypeSpec.classBuilder(className.simpleName).generate()

        FileSpec.builder(className.packageName, className.simpleName)
                .addType(spec.build())
                .build()
                .writeTo(generators.root);
    }

    abstract fun TypeSpec.Builder.generate() : TypeSpec.Builder
}