package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
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

open class MethodGenerator(val generator : UnitGenerator, val builder : FunSpec.Builder) {

    constructor(generator : UnitGenerator, name : String, type : KClass<*>)
            : this(generator, FunSpec.builder(name).returns(type))

    open fun build() = builder.build()

    /**
     * lookup a variable within an expression
     */
    open fun getVariable(name : String) : Variable {
        return generator.getVariable(name)
    }

    /**
     * Lookup variable as function argument
     */
    open fun getParameter(name : String) : Variable {
        return generator.getVariable(name)
    }

    fun getVariable(el : Element) = getVariable(el.name)

    fun Variable.asKlass(): KClass<*> = generator.getKlass(this.type())

    fun addParameters(el : Element?) : MethodGenerator {

        for (arg in el.all("arg")) {
            val param = getParameter(arg["var"]!!.name)
            val type = generator.getKlass(param.type())
            val spec = ParameterSpec.builder(param.getName(), type).build()
            builder.addParameter(spec)
        }

        return this;
    }

    fun CodeBlock.Builder.declVariable(v : Variable) : CodeBlock.Builder {
        add("var %N = ", v.name)
        add(v.initialize(v.asKlass()))
        add("\n")
        return this
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
            "F" -> generator.lineNumber = expr["line"]

            "c" -> comment(expr)

            "b" -> braced(expr)

            "expr","while" -> exprs(expr)

            "arg" -> arg(expr)
            "var" -> variable(expr)
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

    fun CodeBlock.Builder.variable(expr : Element) : CodeBlock.Builder {
        val target = getVariable(expr).targetName()
        add("%N", target)
        return this
    }

    fun CodeBlock.Builder.call(expr : Element) : CodeBlock.Builder {

        var name = expr.name

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

    fun CodeBlock.Builder.code(expr : Element) : CodeBlock.Builder {
        add("\n// code\n\n")
        return this;
    }

    fun CodeBlock.Builder.body(code : Element) : CodeBlock.Builder {

        when(code.getTagName()) {
            "F" -> generator.lineNumber = code["line"]

            "c" -> comment(code)

            "assvar" -> assvar(code)

            "assarr" -> assarr(code)

            "call" -> call(code)

            "goto" -> _goto(code)

            "if" -> _if(code)

            "do"->  _do(code)

            "cycle" -> cycle()

            "exit" -> _exit()

            "return" -> _return()

        }

        return this
    }

    fun CodeBlock.Builder.assvar(expr : Element) {

    }


    fun CodeBlock.Builder.assarr(expr : Element) {

    }


    fun CodeBlock.Builder._goto(expr : Element) {

    }


    fun CodeBlock.Builder._if(expr : Element) {

    }


    fun CodeBlock.Builder.cycle() {

    }

    fun CodeBlock.Builder._exit() {

    }

    fun CodeBlock.Builder._return() {

    }

    fun CodeBlock.Builder._do(expr : Element) {

    }
}
