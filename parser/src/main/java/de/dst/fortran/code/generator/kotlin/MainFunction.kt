package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import de.dst.fortran.code.Local

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

        val main = object : BlockBuilder(this) {
            init {
                if(generator.retval!=null) {
                    locals.put(generator.retval.name, Local(generator.retval.name, Local.Stat.M))
                    //declVariable(generator.retval)
                }

                //for (variable in generator.code.variables) {
                //
                //    if(variable.isLocal() && variable.isModified)
                //        if(variable.name==generator.block.name)
                //            declVariable(generator.retval!!)
                //        else
                //            declVariable(variable)
                //}
            }
        }

        main.addCode(el["code"].children())
        function.addCode(main.build())
        return super.build()
    }
}
