package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.*
import de.dst.fortran.code.Type
import de.dst.fortran.code.TypeDef
import de.dst.fortran.code.Value
import de.dst.fortran.code.Variable
import de.irt.kfor.*
import kotlin.reflect.KClass

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.01.19
 * Time: 19:00
 */

fun Variable?.isProperty() : Boolean {
    val type = this?.typeDef()
    return type?.kind == Value.Kind.PROPERTY
}

fun TypeDef.baseType() : KClass<*> =
        when(type) {
            Type.R8 -> R8::class
            Type.R4 -> R4::class
            Type.I4 -> I4::class
            Type.I2 -> I2::class
            Type.CH -> Ch::class
            Type.STR -> Str::class
            Type.CPX -> Cplx::class
            else ->
                throw RuntimeException(this.toString())
        }

fun Variable.initialize(klass : KClass<*>) = when(klass) {
    Byte::class, Short::class, Int::class, Long::class -> CodeBlock.of("0")
    Float::class -> CodeBlock.of("0.0F")
    Double::class -> CodeBlock.of("0.0")
    Char::class -> CodeBlock.of("' '")
    String::class -> CodeBlock.of("\"\"")
    Boolean::class -> CodeBlock.of("false")
    CRef::class -> CodeBlock.of("%T()", klass)
    else -> {
        val type = this.typeDef().baseType()
        if(this.props.contains(Variable.Prop.ALLOCATABLE))
            when (this.dim.size) {
                1 -> CodeBlock.of("%T.arr()", type)
                2 -> CodeBlock.of("%T.mat()", type)
                3 -> CodeBlock.of("%T.cub()", type)
                else -> CodeBlock.of("%T()", type)
            }
        else
            when (this.dim.size) {
                1 -> CodeBlock.of("%T.arr(%L)", type, this.dim[0])
                2 -> CodeBlock.of("%T.mat(%L, %L)", type, this.dim[0], this.dim[1])
                3 -> CodeBlock.of("%T.cub(%L, %L, %L)", type, this.dim[0], this.dim[1], this.dim[2])
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
        val klass = getKlass(typeDef())
        
        val isAllocated = props.contains(Variable.Prop.ALLOCATABLE)

        //var typeName = klass.asTypeName()
        //if(isAllocated)
        //    typeName = typeName.copy(true)

        val builder = PropertySpec.builder(name, klass)
                .mutable(klass.isMutable() || isAllocated)
                .initializer(initialize(klass))

        return builder.build()
    }

    open fun generate() {

        val spec = TypeSpec.classBuilder(className.simpleName).generate()

        FileSpec.builder(className.packageName, className.simpleName)
                //.addImport("kotlin.math", "*")
                .addType(spec.build())
                .build()
                .writeTo(generators.root);
    }

    abstract fun TypeSpec.Builder.generate() : TypeSpec.Builder
}