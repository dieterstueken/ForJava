package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import de.dst.fortran.code.Context
import de.dst.fortran.code.Entities
import de.dst.fortran.code.TypeDef
import de.dst.fortran.code.Variable
import org.w3c.dom.Element
import kotlin.reflect.KClass

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  15.01.2019 18:39
 * modified by: $Author$
 * modified on: $Date$
 */

class MethodGenerator(val generator : UnitGenerator, variable : Variable) {

    // function parameters
    val parameters = Entities<Variable>(::Variable)

    val builder = FunSpec.builder(variable.name).returns(variable.asKlass())

    fun build() = builder.build()

    /**
     * Either local or by generator
     */
    fun getVariable(name : String) : Variable {
        val parameter : Variable? = parameters.find(name)
        return parameter ?: generator.getVariable(name)
    }

    fun getVariable(el : Element) = getVariable(el.name)

    fun TypeDef.asKlass() : KClass<*> = with(generator) {this@asKlass.asKlass()}

    fun Variable.asKlass() : KClass<*> = type().asKlass()

    fun addParameters(el : Element) : MethodGenerator {

        for (arg in el.all("arg")) {
            val v = parameters.get(arg["var"]!!.name)
            val spec = ParameterSpec.builder(v.getName(), v.asKlass()).build()
            builder.addParameter(spec)
        }

        return this;
    }

    fun defineFunction(el : Element) : MethodGenerator {
        val name = el.name

        val variable = getVariable(name)
        if (variable.context !== Context.FUNCTION)
            throw IllegalArgumentException("undefined function: $name")

        val type = variable.type().asKlass()

        val assarr = el["assarr"]

        addParameters(assarr["args"])
        
        val body = CodeBlock.builder()
                .add("« return ")
                .expr(assarr["expr"])
                .add("\n»")
                .build()

        builder.addCode(body)

        return this;
    }



    fun CodeBlock.Builder.exprs(exprs : Element) : CodeBlock.Builder {
        return exprs(exprs.children())
    }

    fun CodeBlock.Builder.exprs(exprs : Iterable<Element>) : CodeBlock.Builder {
        exprs.forEach{expr(it)}
        return this
    }

    fun CodeBlock.Builder.expr(expr : Element) : CodeBlock.Builder {

        when(expr.getTagName()) {
            "F" -> generator.lineNumber = expr.getAttribute("line");

            "c" -> comment(expr)

            "b" -> braced(expr)

            "expr","while" -> exprs(expr)

            "arg" -> arg(expr)
            "var" -> getVariable(expr)
            "val" -> value(expr)
            "fun" -> call(expr)
            "string" -> addString(expr)

            "add" -> add("+")
            "sub" -> add("-")
            "neg" -> add("-")
            "mul" -> add("*")
            "div" -> add("/")
            "pow" -> add(" pow ")

            "eq" -> add("==")
            "ne" -> add("!=")
            "le" -> add("<=")
            "lt" -> add("<")
            "ge" -> add(">=")
            "gt" -> add(">")

            "and" -> add(" && ")
            "or" -> add(" || ")
            "f"  -> add("\n")

            else -> unknown(expr)
        }

        return this
    }

    fun CodeBlock.Builder.arg(expr : Element) : CodeBlock.Builder {
        // ? assigned function argument
        //val assigned = "true".equals(expr.getAttribute("returned"))
        return exprs(expr)
    }

    fun CodeBlock.Builder.getVariable(expr : Element) : CodeBlock.Builder {
        val name = expr.getAttribute("name")
        // todo: arguments must have precedence
        val target = getVariable(name).targetName() ?: name
        add(target)
        return this
    }

    fun CodeBlock.Builder.call(expr : Element) : CodeBlock.Builder {

        var name = expr.getAttribute("name")

        // conversions
        when(name) {
            "float" -> name = "_float"
            "int" -> name = "_int"
            "cmplx" -> name = "_cpx"
        }

        var args = expr.all("arg")

        add("${name}(")
        var sep = ""

        for (arg in args) {
            add(sep)
            expr(arg)
            sep = ", "
        }

        add(")")

        return this;
    }

    /**
     * parse a constant value
     */
    fun CodeBlock.Builder.value(expr : Element) : CodeBlock.Builder {
        var value = expr.textContent

        when(value) {
            "true" -> add("%L", true)
            "false" -> add("%L", false)
            else -> {
                if(value.contains('.')) {
                    if(value.contains('d')) {
                        value = value.replace('d', 'E')
                        add("%L", value.toDouble())
                    } else
                        add("%L", value.toFloat())
                } else
                    add("%L", value.toInt())
            }
        }

        return this;
    }

    fun CodeBlock.Builder.addString(expr : Element) : CodeBlock.Builder {
        val text = expr.textContent
        if(text.length==1)
            add("%L", text[0])
        else
            add("%S", text)

        return this;
    }

    fun CodeBlock.Builder.comment(expr : Element) : CodeBlock.Builder {
        add("//")
        add(expr.getTextContent())
        add("\n");
        return this;
    }

    fun CodeBlock.Builder.braced(expr : Element) : CodeBlock.Builder {
        add("(")
        exprs(expr)
        add(")")
        return this
    }

    fun CodeBlock.Builder.unknown(expr : Element) : CodeBlock.Builder {
        add("// ?? ")
        add(expr.getTextContent())
        add("\n");
        return this;
    }
}