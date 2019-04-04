package de.dst.fortran.code.generator.kotlin

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

open class MethodGenerator(override val generator : UnitGenerator, val function : FunSpec.Builder)
    : Generator {

    constructor(generator : UnitGenerator, name : String, type : KClass<*>)
            : this(generator, FunSpec.builder(name).returns(type))

    open fun build() = function.build()


    override fun targetName(variable: Variable, asReference: Boolean): String = generator.targetName(variable, asReference)


    /**
     * Lookup variable as function argument
     */
    open fun getParameter(name : String) : Variable {
        return generator.getVariable(name)
    }

    override fun localFunction(name : String) = generator.functions.get(name)

    fun addParameters(el : Element?) : MethodGenerator {

        for (arg in el.all("arg")) {
            val param = getParameter(arg["var"]!!.name)

            // skip pure index variables
            if(!param.isIndex()) {
                val type = generator.getKlass(param.typeDef())
                val spec = ParameterSpec.builder(param.getName(), type).build()
                function.addParameter(spec)
            }
        }

        return this;
    }

}
