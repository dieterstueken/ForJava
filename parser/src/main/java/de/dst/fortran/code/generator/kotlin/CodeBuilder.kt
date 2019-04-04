package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.CodeBlock
import de.dst.fortran.code.Type
import de.dst.fortran.code.Variable
import org.w3c.dom.Element
import kotlin.reflect.KClass

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 03.03.19
 * Time: 20:55
 */
open class CodeBuilder(val method: Generator) {

    val code = CodeBlock.builder()

    open fun build() = code.build()

    // sub expression to evaluate
    fun expr() = ExpressionBuilder(method)

    /**
     * lookup a variable within an expression
     */
    open fun getVariable(name : String) : Variable {
        return method.getVariable(name)
    }

    fun isLocal(name : String) : Boolean {
        val v : Variable? = method.code.variables.find(name)
        if(v!=null && v.isLocal)
            return true;
        else
            return false;
    }

    fun Variable.asKlass(): KClass<*> = method.getKlass(this.typeDef())

    fun asType(name : String) : Type =
        if(name.isNotEmpty())
            Type.valueOf(name)
        else
            Type.NONE

    fun targetName(variable : Variable, asReference: Boolean)
            = method.targetName(variable, asReference)

    fun codeLine(el : Element) = method.buildCodeLine(code, el)

    fun contLine(el : Element) = code.add("\n    ")

    fun comment(expr : Element) {
        var text : String? = expr.getTextContent()
        if(text!=null && text.isNotEmpty()) {
            code.add("// %L\n", text.trim())
        } else
            code.add("\n");
    }

    fun commentLine(expr : Element) {
        return comment(expr)
    }

    fun unknown(expr : Element) {
        code.add("/*< ")
        code.add(expr.tagName)
        code.add(">*/");
    }
}