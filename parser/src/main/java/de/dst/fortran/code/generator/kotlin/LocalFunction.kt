package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.FunSpec
import de.dst.fortran.code.Context
import de.dst.fortran.code.Entities
import de.dst.fortran.code.Variable
import org.w3c.dom.Element
import kotlin.reflect.KClass

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 19.01.19
 * Time: 13:16
 */
class LocalFunction(generator : UnitGenerator, val element : Element, type : KClass<*>)
    : MethodGenerator(generator, element.name, type) {

    companion object {
        operator fun invoke(generator : UnitGenerator, element: Element) : LocalFunction {
            val variable = generator.getVariable(element.name)

            if (variable.context !== Context.FUNCTION)
                throw IllegalArgumentException("undefined function: ${element.name}")

            // ask generator
            val type = generator.getKlass(variable.type())

            return LocalFunction(generator, element, type)
        }
    }

    // function parameters
    val parameters = Entities<Variable>(::Variable)

    override fun getParameter(name: String) = parameters.get(name)

    override fun getVariable(name: String) = parameters.find(name)!!// ?: generator.getVariable(name)

    override fun build(): FunSpec {

        val assarr = element["assarr"]

        addParameters(assarr["args"])

        val expr = ExpressionBuilder(this)

        expr.code.add("«return ")
        expr.addExprel(assarr["expr"])
        expr.code.add("\n»")

        function.addCode(expr.build())

        return super.build();
    }
}