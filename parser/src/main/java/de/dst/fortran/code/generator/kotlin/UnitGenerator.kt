package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import de.dst.fortran.code.CodeElement
import de.dst.fortran.code.Context
import de.dst.fortran.code.Entities
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

    var lineNumber : Any = block.line

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

    override fun generate() {
        try {
            super.generate();
        } catch (error: Throwable) {
            throw RuntimeException("error building ${code.path}.${code.name} at line $lineNumber  ", error);
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

        for (function in block.element()["functions"].all("function")) {
            addFunction(localFunction(function))
        }

        addFunction(mainFunction())

        return this;
    }

    fun localFunction(el: Element): FunSpec {

        val variable = getVariable(el.name)

        if (variable.context !== Context.FUNCTION)
            throw IllegalArgumentException("undefined function: ${el.name}")

        val assarr = el["assarr"]

        return object : MethodGenerator(this, el.name, getVariable(el.name).asKlass()) {

            // function parameters
            val parameters = Entities<Variable>(::Variable)

            override fun getParameter(name: String) = parameters.get(name)

            override fun getVariable(name: String) = parameters.find(name) ?: this@UnitGenerator.getVariable(name)

            override fun build(): FunSpec {

                addParameters(assarr["args"])

                builder.addCode(CodeBlock.builder()
                        .add("« return ")
                        .expr(assarr["expr"])
                        .add("\n»")
                        .build())

                return super.build();
            }
        }.build()
    }

    fun mainFunction() : FunSpec {

        val element = block.element()
        val type = getKlass()

        // variable
        val retval : Variable? = if(Unit::class==type) null else
            Variable("retval")
                .type(code.type().type)
                .prop(Variable.Prop.ASSIGNED)
                .prop(Variable.Prop.RETURNED)

        return object : MethodGenerator(this, "invoke", type) {

            override fun getVariable(name: String): Variable {

                // replace by functions retval
                if(name == element.name)
                    return retval!!

                return this@UnitGenerator.getVariable(name)
            }

            override fun build(): FunSpec {

                val el = block.element()

                addParameters(el["args"])

                val code = CodeBlock.builder()

                if (retval!=null) {
                    builder.returns(type)
                    code.declVariable(retval)
                }

                code.code(el["code"])

                if (retval!=null) {
                    code.add("return retval\n")
                }

                builder.addCode(code.build())

                return super.build();
            }
        }.build()
    }

}

