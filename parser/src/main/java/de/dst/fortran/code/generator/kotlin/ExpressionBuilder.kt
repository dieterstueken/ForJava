package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.CodeBlock
import de.dst.fortran.code.Type
import de.dst.fortran.code.Variable
import org.w3c.dom.Element
import kotlin.reflect.KClass

open class ExpressionBuilder(val method: MethodGenerator) {

    val code = CodeBlock.builder()

    open fun build() = code.build()

    /**
     * lookup a variable within an expression
     */
    open fun getVariable(name : String) : Variable {
        return method.getVariable(name)
    }

    fun isLocal(name : String) : Boolean {
        val v : Variable? = method.generator.code.variables.find(name)
        if(v!=null && v.isLocal)
            return true;
        else
            return false;
    }

    fun Variable.asKlass(): KClass<*> = method.generator.getKlass(this.type())

    fun targetName(variable : Variable, asReference: Boolean) : String {
        var target = ""

        if(variable.context!=null) {
            if(variable.context!=method.generator.code)
                target = "${variable.context.name}."
        }

        target += "%N"

        if(!asReference && variable.isProperty() && !variable.isCpx())
            target += ".v"

        return target
    }

    fun addExpr(expr : List<Element>) {

        var add : (Element) -> Unit = this::addExpr

        for (elem in expr) {
            if(elem.getTagName().equals("pow")) {
                code.add(".pow")
                add = {
                    if (it.getTagName() == "b")
                        braced(it)
                    else {
                        code.add("(")
                        addExpr(it)
                        code.add(")")
                    }
                    // return to normal procedure
                    add = this::addExpr
                }
            } else
                add(elem)
        }
    }


    fun addExpr(expr : Element) {

        val tag = expr.getTagName()
        when(tag) {
            "F" -> codeLine(expr)
            "f"  -> contLine(expr)
            "c" -> comment(expr)
            "C" -> commentLine(expr)

            "b" -> braced(expr)

            "expr"-> addExpr(expr.children())

            "arg" -> arg(expr)
            "var" -> variable(expr)
            "val" -> value(expr)
            "fun" -> function(expr)
            "string" -> addString(expr)
            "cat" -> concat(expr)

            "add" -> code.add("+")
            "sub" -> code.add("-")
            "neg" -> code.add("-")
            "mul" -> code.add("*")
            "div" -> code.add("/")
            "pow" -> code.add(" pow ")

            "eq" -> code.add("==")
            "ne" -> code.add("!=")
            "le" -> code.add("<=")
            "lt" -> code.add("<")
            "ge" -> code.add(">=")
            "gt" -> code.add(">")

            "and" -> code.add(" && ")
            "or" -> code.add(" || ")

            else -> unknown(expr)
        }
    }

    fun arg(expr : Element) {
        // ? assigned function argument
        //val assigned = "true".equals(expr.getAttribute("returned"))
        return addExpr(expr.children())
    }

    fun variable(expr : Element) {
        val variable = method.getVariable(expr)
        val asReference = expr.attributes["returned"]=="true" || variable.type().type== Type.STR
        val target = targetName(variable, asReference)
        code.add("$target", variable.name)
    }

    fun function(expr : Element) {

        var name : String = expr.name

        // conversions
        when(name) {
            "float" -> name = "toReal"
            "int" -> name = "toInt"
            "cmplx" -> name = "Cpx"
        }

        if(expr.attributes["scope"] == "array") {
            val variable = getVariable(name)
            name = variable.name
            val target = targetName(variable, true)
            if(expr.attributes["ref"]=="true") {
                code.add("$target(", name)
                addArgs(expr)
                code.add(")")
            } else {
                code.add("$target[", name)
                addArgs(expr)
                code.add("]")
            }
        } else {
            code.add("%N(", name)
            addArgs(expr)
            code.add(")")
        }
    }

    fun addArgs(args : Element) {
        addArgs(args.all("arg"))
    }

    fun addArgs(args : List<Element>, sep : String = ", ") {
        var s = ""
        for (arg in args) {
            code.add(s)
            addExpr(arg)
            s = sep
        }
    }

    /**
     * parse a constant value
     */
    fun value(expr : Element) {
        var value = expr.textContent

        when(value) {
            "true" -> code.add("%L", true)
            "false" -> code.add("%L", false)
            else -> {
                if(value.contains('.')) {
                    if(value.contains('d')) {
                        value = value.replace('d', 'E')
                        code.add("%L", value.toDouble())
                    } else
                        code.add("%L", value.toFloat())
                } else
                    code.add("%L", value.toInt())
            }
        }
    }

    fun addString(expr : Element) {
        val text = expr.textContent
        if(text.length==1)
            code.add("'%L'", text[0])
        else
            code.add("%S", text)
    }

    fun concat(expr : Element) {
        code.add(" + ");
        addExpr(expr.children());
    }

    fun codeLine(el : Element) = method.buildCodeLine(code, el)

    fun contLine(el : Element) = code.add("\n    ")

    fun comment(expr : Element) {
        var text : String? = expr.getTextContent()
        if(text!=null && text.isNotEmpty()) {
            code.add("//%L\n", text)
        } else
            code.add("\n");
    }

    fun commentLine(expr : Element) {
        return comment(expr)
    }

    fun braced(expr : Element) {
        code.add("(")
        addExpr(expr.children())
        code.add(")")
    }

    fun unknown(expr : Element) {
        code.add("/*< ")
        code.add(expr.tagName)
        code.add(">*/");
    }

}