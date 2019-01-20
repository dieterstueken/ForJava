package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import de.dst.fortran.code.Variable
import kotlin.reflect.KClass

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 19.01.19
 * Time: 13:37
 */

class MainFunction(generator : UnitGenerator, val type : KClass<*>)
    : MethodGenerator(generator, "invoke", type) {
    
    constructor(generator : UnitGenerator) : this(generator, generator.getKlass())

    init {
        function.addModifiers(KModifier.OPERATOR)
    }

    // variable
    val retval : Variable? = if(Unit::class==type) null else
        Variable("retval")
            .type(generator.code.type().type)
            .prop(Variable.Prop.ASSIGNED)
            .prop(Variable.Prop.RETURNED)

    override fun getVariable(name: String): Variable {

        // replace by retval of function
        if(name == generator.block.name)
            return retval!!

        return generator.getVariable(name)
    }

    override fun build(): FunSpec {

        val el = generator.block.element()

        addParameters(el["args"])

        val code = with(CodeBuilder(this)) {

            if (retval != null) {
                function.returns(type)
                declVariable(retval)
            }

            for (variable in generator.code.variables) {
                if(variable.isLocal() && variable.isModified && variable.name!=generator.block.name)
                    declVariable(variable)
            }

            body(el["code"])

            build()
        }
        //if (retval!=null) {
        //    code.add("return retval\n")
        //}

        function.addCode(code)

        return super.build();
    }

    override fun addReturn(code : CodeBlock.Builder) : CodeBlock.Builder = if(retval!=null)
        code.add("return %N\n", retval.name)
    else
        super.addReturn(code)

}
