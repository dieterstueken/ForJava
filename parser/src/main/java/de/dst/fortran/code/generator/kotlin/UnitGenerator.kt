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
    : ClassGenerator(generators, "function", className) {

    var lineNumber : Int = block.line.toInt()

    fun setLineNumber(line : String?) {
        lineNumber = if(line==null || line.isEmpty()) 0 else line.toInt()
        debug(lineNumber)
    }

    fun debug(linenum : Int) : Boolean {
        if(block.name == "corfa" && lineNumber==42)
            return true
        else
            return false
    }

    val code = block.code()

    fun getKlass(): KClass<*> = getKlass(code.type())

    fun Variable.asKlass(): KClass<*> = getKlass(this.type())

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

    override fun toString() : String = "${code.path}.${code.name}:${lineNumber}"

    override fun generate() {
        try {
            super.generate();
        } catch (error: Throwable) {
            throw RuntimeException("error building ${this}", error)
        }
    }

    fun getVariable(name: String) = code.variables.find(name)!!

    fun Variable.isMember(): Boolean = context == null && !isPrimitive() && name != code.name

    override fun TypeSpec.Builder.generate(): TypeSpec.Builder {

        superclass(Fortran::class)

        primaryConstructor(FunSpec.constructorBuilder()
                .addParameter("units", Units::class)
                .build())

        addSuperclassConstructorParameter(CodeBlock.of("units"))

        for (common in code.commons) {
            addProperty(generators.asProperty(common))
        }

        for (unit in code.blocks) {
            addProperty(generators.asProperty(unit))
        }

        for (variable in code.variables) {
            if (variable.isMember())
                addProperty(variable.asProperty())
        }

        for (func in block.element()["functions"].all("function")) {
            addFunction(localFunction(func))
        }

        addFunction(mainFunction())

        return this;
    }

    fun localFunction(func: Element) = LocalFunction(this, func).build();

    fun mainFunction() = MainFunction(this).build()
}

