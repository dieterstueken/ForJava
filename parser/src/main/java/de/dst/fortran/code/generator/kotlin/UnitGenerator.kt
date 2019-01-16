package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import de.dst.fortran.code.CodeElement
import de.dst.fortran.code.Variable
import de.irt.kfor.Fortran
import de.irt.kfor.Units
import org.w3c.dom.Element
import kotlin.reflect.KClass

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.01.19
 * Time: 18:17
 */

fun CodeElement.camelName(): String {

    var name = this.code().name;

    var c = name[0];
    if (c.isUpperCase())
        return name;

    return c.toUpperCase() + name.substring(1)
}

class UnitGenerator(generators : CodeGenerators, val block : CodeElement, className : ClassName)
    : CodeGenerator(generators, "function", className) {

    var lineNumber = "";

    val code = block.code()

    fun getKlass() : KClass<*> = asKlass(code.type())

    companion object {
        fun create(generators: CodeGenerators, element: CodeElement): UnitGenerator {
            val className = ClassName(generators.packageRoot + '.' + element.code().path, element.camelName())
            return UnitGenerator(generators, element, className)
        }

        fun blocks(generators: CodeGenerators): CodeBlocks<CodeElement> {
            return object : CodeBlocks<CodeElement>(generators) {
                override fun generate(block: CodeElement) = UnitGenerator.create(generators, block);
            }
        }
    }

    override fun generate() {
        try {
            super.generate();
        } catch(error : Throwable) {
            throw RuntimeException("error building ${code.name} at line $lineNumber  ", error);
        }
    }

    override fun TypeSpec.Builder.generate() : TypeSpec.Builder {

        val properties = code.commons.asSequence().map(generators::asProperty).asIterable()

        val units = code.blocks.asSequence().map(generators::asProperty).asIterable()

        val members = code.variables.asSequence()
                .filter(Variable::isMember)
                .map { it.asProperty() }
                .asIterable()

        // instrinsic functions
        val functions = block.element()
                .get("functions")
                .all("function")
                .map(::asFunction)
                .asIterable()

        return superclass(Fortran::class)
                .primaryConstructor(FunSpec.constructorBuilder()
                        .addParameter("units", Units::class)
                        .build())
                .addSuperclassConstructorParameter(CodeBlock.of("units"))
                .addProperties(properties)
                .addProperties(units)
                .addProperties(members)
                .addFunctions(functions)
                .addFunction(mainFunction())
    }

    fun mainFunction() : FunSpec = MethodGenerator(this, block.name, getKlass())
            .mainFunction(block.element())
            .build()

    fun getVariable(name : String)  = code.variables.find(name)!!

    fun asFunction(el : Element) : FunSpec {

        val generator = MethodGenerator(this, getVariable(el.name))

        return generator.localFunction(el).build()
    }
}

