package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.*
import de.dst.fortran.code.CodeElement
import de.irt.kfor.Fortran
import de.irt.kfor.Units

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.01.19
 * Time: 18:17
 */

class UnitGenerator(generators : CodeGenerators, className : ClassName, element : CodeElement)
    : CodeGenerator<CodeElement>(generators, className, element)
{
    override val initialize = "function(%T::class)"

    val code = block.code()

    companion object {
        fun create(generators : CodeGenerators, element : CodeElement) : UnitGenerator {
            val className = ClassName(generators.packageRoot + '.' + element.code().path, element.camelName())
            return UnitGenerator(generators, className, element)
        }

        fun CodeElement.camelName() : String {

             var name = this.code().name;

             var c = name[0];
             if(c.isUpperCase())
                 return name;

             return c.toUpperCase() + name.substring(1)
        }

        fun blocks(generators : CodeGenerators) : CodeBlocks<CodeElement> {
            return object: CodeBlocks<CodeElement>(generators) {
                override fun generate(block: CodeElement) = UnitGenerator.create(generators, block);
            }
        }
    }

    override fun generate() {

        FileSpec.builder(className.packageName, className.simpleName)
                .addType(TypeSpec.classBuilder(className.simpleName)
                                .superclass(Fortran::class)
                                .primaryConstructor(FunSpec.constructorBuilder()
                                        .addParameter("units", Units::class)
                                        .build())
                        .addSuperclassConstructorParameter(CodeBlock.of("units"))
                        .addProperties(properties())
                        .addProperties(units())
                        .addProperties(members())
                        .build()
                )
                .build()
                .writeTo(generators.root)
    }

    fun properties() = code.commons.map(generators::asProperty)

    fun units() = code.blocks.map(generators::asProperty)
    
    fun members() = code.variables
            .filter{it.context==null}
            .filter{!it.isPrimitive}
            .map{it.asProperty()}
}

