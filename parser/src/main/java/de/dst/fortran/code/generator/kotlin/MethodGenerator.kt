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

    fun targetName(variable : Variable, asReference: Boolean) : String {
        var target = ""

        if(variable.context!=null) {
            if(variable.context!=generator.code)
                target = "${variable.context.name}."
        }

        target += variable.name

        if(!asReference && variable.isProperty())
            target += ".v"

        return target
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

    fun CodeBlock.Builder.addExpr(exprs : Iterable<Element>) : CodeBlock.Builder {
        for (expr in exprs) {
            addExpr(expr)
        }
        return this
    }

    fun CodeBlock.Builder.addExpr(expr : Element) : CodeBlock.Builder {

        val tag = expr.getTagName()
        when(tag) {
            "F" -> codeLine(expr)
            "f"  -> contLine(expr)
            "c" -> comment(expr)
            "C" -> commentLine(expr)

            "b" -> braced(expr)

            "expr","while" -> addExpr(expr.children())

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


            else -> unknown(expr)
        }

        return this
    }

    fun CodeBlock.Builder.arg(expr : Element) : CodeBlock.Builder {
        // ? assigned function argument
        //val assigned = "true".equals(expr.getAttribute("returned"))
        return addExpr(expr.children())
    }

    fun CodeBlock.Builder.variable(expr : Element) : CodeBlock.Builder {
        val asReference = expr.attributes["returned"]=="true"
        val target = targetName(getVariable(expr), asReference)
        add("%N", target)
        return this
    }

    fun CodeBlock.Builder.call(expr : Element) : CodeBlock.Builder {

        var name : String = expr.name

        // conversions
        when(name) {
            "float" -> name = "toReal"
            "int" -> name = "toInt"
            "cmplx" -> name = "toComplex"
        }

        if(expr.attributes["scope"] == "array") {
            name = targetName(getVariable(name), true)
            return add("%N[", name).addArgs(expr).add("]")
        } else
            return add("%N(", name).addArgs(expr).add(")")
    }

    fun CodeBlock.Builder.addArgs(args : Element) : CodeBlock.Builder {
        var sep = ""
        for (arg in args.all("arg")) {
            add(sep)
            addExpr(arg)
            sep = ", "
        }
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

    open fun buildCodeLine(builder : CodeBlock.Builder, el : Element) {
        val ln : String = el.getAttribute("line")
        generator.lineNumber = ln
    }

    fun CodeBlock.Builder.codeLine(el : Element) = buildCodeLine(this, el)

    fun CodeBlock.Builder.contLine(el : Element) = add("\n    ")

    fun CodeBlock.Builder.comment(expr : Element) : CodeBlock.Builder {
        var text : String? = expr.getTextContent()
        if(text!=null && text.isNotEmpty()) {
            add("//%L\n", text)
        } else
            add("\n");

        return this;
    }

    fun CodeBlock.Builder.commentLine(expr : Element) : CodeBlock.Builder {
        return comment(expr)
    }

    fun CodeBlock.Builder.braced(expr : Element) : CodeBlock.Builder {
        add("(")
        addExpr(expr.children())
        add(")")
        return this
    }

    fun CodeBlock.Builder.unknown(expr : Element) : CodeBlock.Builder {
        add("/*< ")
        add(expr.name)
        add(">*/");
        return this;
    }
}
