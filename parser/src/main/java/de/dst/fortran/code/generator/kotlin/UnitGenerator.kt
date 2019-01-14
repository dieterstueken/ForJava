package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.*
import de.dst.fortran.code.BlockElement
import de.irt.kfor.Fortran
import de.irt.kfor.Units

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.01.19
 * Time: 18:17
 */

class UnitGenerator(generators : CodeGenerators, className : ClassName, val element : BlockElement)
    : CodeGenerator(generators, className)
{
    companion object {
        fun create(generators : CodeGenerators, element : BlockElement) : UnitGenerator {
            val className = ClassName(generators.packageRoot + '.' + element.block().path, element.camelName())
            return UnitGenerator(generators, className, element)
        }

        fun BlockElement.camelName() : String {

             var name = this.block().name;

             var c = name[0];
             if(c.isUpperCase())
                 return name;

             return c.toUpperCase() + name.substring(1)
        }
    }

    override fun build() {

        FileSpec.builder(className.packageName, className.simpleName)
                .addType(TypeSpec.classBuilder(className.simpleName)
                                .superclass(Fortran::class)
                                .primaryConstructor(FunSpec.constructorBuilder()
                                                        .addParameter("units", Units::class)
                                                        .build())
                                                .addSuperclassConstructorParameter(CodeBlock.of("units"))
                                                .addProperties(properties())
                                                .addProperties(units())
                                                .build()
                )
                .build()
                .writeTo(generators.root)
    }

    fun properties() = element.block().commons.map{
        val type : TypeName = generators.commons.get(it.name)!!
        PropertySpec.builder(it.name, type)
                .initializer("unit(%T::class)", type)
                .build()
    }

    fun units() = element.block().blocks.asIterable().map{
        val type : TypeName = generators.units.get(it.name)!!.className
        PropertySpec.builder(it.name, type)
                .initializer("unit(%T::class)", type)
                .build()
    }
}

