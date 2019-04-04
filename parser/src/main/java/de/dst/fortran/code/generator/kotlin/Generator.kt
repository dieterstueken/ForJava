package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.CodeBlock
import de.dst.fortran.code.Code
import de.dst.fortran.code.CodeElement
import de.dst.fortran.code.TypeDef
import de.dst.fortran.code.Variable
import org.w3c.dom.Element
import kotlin.reflect.KClass

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.04.19
 * Time: 19:56
 */
interface Generator {

    val generator : UnitGenerator

    val block : CodeElement get() = generator.block

    val code : Code get() = block.code()

    val retval : Variable? get() = null

    fun getKlass(type : TypeDef?) : KClass<*> = generator.getKlass(type)

    fun getVariable(name : String) : Variable {
        return generator.getVariable(name)
    }

    fun getVariable(el : Element) = getVariable(el.name)

    fun targetName(variable : Variable, asReference: Boolean) : String = generator.targetName(variable, asReference)

    fun localFunction(name : String) : LocalFunction? {
        return null
    }

    fun buildCodeLine(builder : CodeBlock.Builder, el : Element) = generator.setLineNumber(el.getAttribute("line"))
}