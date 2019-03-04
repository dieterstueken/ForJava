package de.dst.fortran.code.generator.kotlin

import de.dst.fortran.code.Type
import org.w3c.dom.Element

open class ExpressionBuilder(method: MethodGenerator) : CodeBuilder(method) {

    var type : Type? = null

    fun updateType(other : Type?) {
        type = type.or(other)
    }

    fun popType() : Type? {
        val tmp = type
        type = null;
        return tmp
    }

    fun addExprs(expr : Element) : ExpressionBuilder {
        return addExprs(expr.children())
    }

    fun addExprs(expr : List<Element>) : ExpressionBuilder {

        type = popType()

        for (el in expr) {
            addExpr(el)
        }

        updateType(type)

        return this
    }

    // add a single expression element
    fun addExpr(elem : Element) : ExpressionBuilder {

        val tag = elem.getTagName()
        when(tag) {
            "F" -> codeLine(elem)
            "f"  -> contLine(elem)
            "c" -> comment(elem)
            "C" -> commentLine(elem)

            "b" -> braced(elem)
            "fun" -> function(elem)
            "expr"-> addExprs(elem)

            "neg" -> {
                code.add("-")
                addExprs(elem)
            }
            
            "prod" -> addExprs(elem)
            "sum" -> addExprs(elem)

            "arg" -> arg(elem)
            "var" -> variable(elem)
            "val" -> value(elem)
            "string" -> addString(elem)
            "cat" -> concat(elem)

            "add" -> code.add("+")
            "sub" -> code.add("-")
            "mul" -> code.add("*")
            "div" -> code.add("/")
            "pow" -> pow(elem)

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

        return this
    }

    fun arg(expr : Element)  : ExpressionBuilder {
        // ? assigned function argument
        //val assigned = "true".equals(expr.getAttribute("returned"))
        return addExprs(expr)
    }

    fun variable(expr : Element) {
        val variable = method.getVariable(expr)
        val asReference = expr.attributes["returned"]=="true" || variable.type().type== Type.STR
        val target = targetName(variable, asReference)
        code.add("$target", variable.name)

        updateType(variable.type().type)
    }

    fun pow(expr : Element) {
        code.add("pow(");
        addArgs(expr.children())
        code.add(")")

        updateType(Type.R8)
    }

    fun function(element : Element) {

        var name : String = element.name

        if(element.attributes["scope"] == "array") {
            val variable = getVariable(name)
            name = variable.name
            val target = targetName(variable, true)

            val type = popType()

            if(element.attributes["ref"]=="true") {
                code.add("$target(", name)
                addArgs(element)
                code.add(")")
            } else {
                code.add("$target[", name)
                addArgs(element)
                code.add("]")
            }

            this.type = type.or(variable.type)
        } else {

            // conversions
            when(name) {
                "float" -> name = "real"
                "int" -> name = "intg"
                "cmplx" -> name = "cplx"
            }

            val expr = expr()

            code.add("%N(", name)
            code.add(expr.addArgs(element).build())
            code.add(")")

            val type = when {
                name.equals("min") || name.equals("max") -> expr.type
                name.startsWith('c') -> Type.CPX
                else -> Type.intrinsic(name)
            }

            updateType(type)
        }
    }

    fun addArgs(args : Element) : ExpressionBuilder {
        return addArgs(args.all("arg"))
    }

    fun addArgs(args : List<Element>, sep : String = ", ") : ExpressionBuilder {

        // join all types
        var type : Type? = null

        var s = ""
        for (arg in args) {
            code.add(s)
            this.type = null
            addExpr(arg)
            type = type.or(this.type)
            s = sep
        }

        this.type = type

        return this;
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
    fun value(expr : Element) {
        var value = toValue(expr)
        code.add("%L", value)
        updateType(typeOf(value::class))
    }

    fun addString(expr : Element) {
        val text = expr.textContent
        if(text.length==1) {
            code.add("'%L'", text[0])
            updateType(Type.CH)
        } else {
            code.add("%S", text)
            updateType(Type.STR)
        }
    }

    fun concat(expr : Element) {
        code.add(" + ")
        addExprs(expr)
        updateType(Type.STR)
    }


    fun braced(expr : Element) {
        code.add("(")
        addExprs(expr)
        code.add(")")
    }


}