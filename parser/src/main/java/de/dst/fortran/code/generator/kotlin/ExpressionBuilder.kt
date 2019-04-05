package de.dst.fortran.code.generator.kotlin

import de.dst.fortran.code.Type
import org.w3c.dom.Element
import java.util.function.Predicate

val MIN_MAX = Regex( "a?(min|max)\\d?")

val IS_ARG : Predicate<Element> = Predicate {it.tagName=="arg"}

val NOT_AN_INDEX : Predicate<Element> = Predicate{it.tagName=="arg" && it.attributes["index"].isEmpty() }

open class ExpressionBuilder(method: Generator) : CodeBuilder(method) {

    fun addExprs(expr : Element) = addExprs(expr.children())

    fun addExprs(expr : List<Element>) : Type {

        var type = Type.NONE

        for (el in expr) {
            type *= addExpr(el)
        }

        return type
    }

    // add a single expression element
    fun addExpr(elem : Element) : Type {

        val tag = elem.getTagName()

        var type = Type.NONE

        when(tag) {
            "F" -> codeLine(elem)
            "f"  -> contLine(elem)
            "c" -> comment(elem)
            "C" -> commentLine(elem)

            "b" -> type = braced(elem)
            "fun" -> type = function(elem)
            "expr"-> type = addExprs(elem)

            "neg" -> {
                code.add("-")
                type = addExprs(elem)
            }
            
            "prod" -> type = addExprs(elem)
            "sum" -> type = addExprs(elem)

            "arg" -> type = arg(elem)
            "var" -> type = variable(elem)
            "val" -> type = value(elem)
            "string" -> type = addString(elem)
            "cat" -> type = concat(elem)

            "cycle" -> code.add("continue")
            "exit" -> code.add("break")

            "add" -> code.add("+")
            "sub" -> code.add("-")
            "mul" -> code.add("*")
            "div" -> code.add("/")
            "pow" -> type = pow(elem)

            "eq" -> code.add("==")
            "ne" -> code.add("!=")
            "le" -> code.add("<=")
            "lt" -> code.add("<")
            "ge" -> code.add(">=")
            "gt" -> code.add(">")

            "and" -> code.add(" && ")
            "or" -> code.add(" || ")

            else -> unknown(elem)
        }

        return type
    }

    fun arg(expr : Element)  : Type {
        // ? assigned function argument
        //val assigned = "true".equals(expr.getAttribute("returned"))
        return addExprs(expr)
    }

    fun variable(expr : Element) : Type {
        val variable = method.getVariable(expr)
        val asReference = expr.attributes["returned"]=="true" || variable.typeDef().type== Type.STR
        val target = targetName(variable, asReference)
        code.add(target, variable.name)

        return variable.typeDef().type
    }

    fun pow(expr : Element) : Type {
        code.add("pow(");
        addArgs(expr.children())
        code.add(")")

        return Type.R8
    }

    fun function(element : Element) : Type {

        var name : String = element.name
        val scope = element.attributes["scope"]

        if(scope == "array") {
            val variable = getVariable(name)
            name = variable.name
            val target = targetName(variable, true)

            if(element.attributes["ref"]=="true") {
                code.add("$target(", name)
                addArgs(element)
                code.add(")")
            } else {
                code.add("$target[", name)
                addArgs(element)
                code.add("]")
            }

            return variable.type
        } else {

            // conversions
            when(name) {
                "float" -> name = "real"
                "int" -> name = "intg"
                "cmplx" -> name = "cplx"
            }

            var filter = if(scope==method.block.name) NOT_AN_INDEX else IS_ARG

            val expr = expr()
            val args = element.all(filter)
            var type = expr.addArgs(args)

            code.add("%N(", name)
                    .add(expr.build())
                    .add(")")

            // possible local function
            val lf = method.localFunction(name)
            if(lf!=null)
                return lf.variable.type

            val rtype = asType(element.attributes["type"])
            if(rtype!=Type.NONE)
                return rtype;

            return when {
                MIN_MAX.matches(name) -> type
                // some non complex exceptions
                name=="cos" -> Type.R8
                name=="cabs" -> Type.R8
                name.startsWith('c') -> Type.CPX
                else -> Type.intrinsic(name)
            }
        }
    }

    fun addfArgs(element : Element) : ExpressionBuilder {
        val args = element.all(NOT_AN_INDEX)
        addArgs(args)
        return this
    }

    fun addArgs(args : Element) : Type {
        return addArgs(args.all("arg"))
    }

    fun addArgs(args : List<Element>, sep : String = ", ") : Type {

        // join all types
        var type : Type = Type.NONE

        var s = ""
        for (arg in args) {
            if(s.isNotEmpty())
                code.add(s)

            val t = addExpr(arg)

            if(t==Type.NONE) {
                // no value added
                s = "";
            }
            else {
                type *= t
                s = sep
            }
        }

        return type;
    }

    /**
     * Extract dim values from list
     */
    fun addValues(values : MutableList<Element>, dim : Int) : ExpressionBuilder {
        var s = ""
        var n = 0
        while(n<dim && values.isNotEmpty()) {

            if(s.isNotEmpty())
                code.add(s)

            val value = values.removeAt(0)
            val t = addExpr(value)

            if(t== Type.NONE) {
                // no value added
                s = "";
            }
            else {
                s = ", "
                ++n;
            }
        }

        if(n<dim)
            throw RuntimeException("data values underflow: ${dim-n}")

        return this
    }

    fun toValue(expr : Element) : Any {
        var text = expr.textContent

        return when(text) {
            "true" -> true
            "false" -> false
            else -> {
                if(text.contains('.')) {
                    if(text.contains('d')) {
                        text = text.replace('d', 'E')
                        text.toDouble()
                    } else
                        text.toFloat()
                } else
                    text.toInt()
            }
        }
    }

    /**
     * parse a constant value
     */
    fun value(expr : Element) : Type {
        val value = toValue(expr)
        code.add("%L", value)
        return typeOf(value::class)
    }

    fun addString(expr : Element) : Type {
        val text = expr.textContent
        if(text.length==1) {
            code.add("'%L'", text[0])
            return Type.CH
        } else {
            code.add("%S", text)
            return Type.STR
        }
    }

    fun concat(expr : Element) : Type {
        code.add(" + ")
        addExprs(expr)
        return Type.STR
    }


    fun braced(expr : Element)  : Type {
        code.add("(")
        val type = addExprs(expr)
        code.add(")")

        return type
    }
}