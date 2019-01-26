package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 19.01.19
 * Time: 13:37
 */

class MainFunction(generator : UnitGenerator)
    : MethodGenerator(generator, "invoke", generator.type) {

    init {
        function.addModifiers(KModifier.OPERATOR)
        if (generator.retval != null)
            function.returns(generator.type)
    }

    override fun getVariable(name: String) = generator.getVariable(name)

    override fun build(): FunSpec {

        val el = generator.block.element()

        addParameters(el["args"])

        val code = object : BlockBuilder(this) {
            init {
                for (variable in generator.code.variables) {

                    if(variable.isLocal() && variable.isModified)
                        if(variable.name==generator.block.name)
                            declVariable(generator.retval!!)
                        else
                            declVariable(variable)
                }
            }

            override fun build() : CodeBlock {
                body(el["code"])
                return super.build()
            }
        }

        function.addCode(code.build())

        return super.build();
    }
}
