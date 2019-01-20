package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import de.dst.fortran.code.Variable
import org.w3c.dom.Element
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
        builder.addModifiers(KModifier.OPERATOR)
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

        val code = CodeBlock.builder()

        if (retval!=null) {
            builder.returns(type)
            code.declVariable(retval)
        }

        code.body(el["code"])

        //if (retval!=null) {
        //    code.add("return retval\n")
        //}

        builder.addCode(code.build())

        return super.build();
    }
    
    fun CodeBlock.Builder.body(elem : Element) : CodeBlock.Builder {
        return addCode(elem.children())
    }

    fun CodeBlock.Builder.addCode(code : Iterable<Element>) : CodeBlock.Builder {

        for (child in code) {
            addCode(child)
        }

        return this;
    }

    fun CodeBlock.Builder.addCode(elem : Element) : CodeBlock.Builder {
        val tag = elem.getTagName()
        when(tag) {
            "F" -> codeLine(elem)
            "f"  -> contLine(elem)
            "c" -> comment(elem)
            "C" -> commentLine(elem)

            "assvar" -> assvar(elem)

            "assarr" -> assarr(elem)

            "call" -> call(elem)

            "goto" -> _goto(elem)

            "if" -> _if(elem)
            "do"->  _do(elem)

            "cycle" -> cycle()

            "exit" -> _exit()

            "return" -> _return()

            else -> unknown(elem)
        }

        return this
    }

    override fun buildCodeLine(builder : CodeBlock.Builder, el : Element) {
        super.buildCodeLine(builder, el)
    }

    var assigned = mutableListOf<String>()

    fun isAssigned(variable : Variable) : Boolean {
        for (name in assigned) {
            if(name==variable.name)
                return true
        }

        return false;
    }

    fun assign(variable : Variable) : Boolean {
        if(isAssigned(variable))
            return false

        assigned.add(variable.name)
        return true;
    }

    fun isUndefined(variable : Variable) = variable.isLocal() && assign(variable)

    fun CodeBlock.Builder.assvar(el : Element) {
        val variable = getVariable(el.name);
        val target = targetName(variable, false)

        val def = when {
            !isUndefined(variable) -> "«%N = "
            variable.wasModified() -> "«var %N = "
            else -> "«val %N = "
        }

        add(def, target)
                .addExpr(el.children())
                .add("\n»")
    }
    
    fun CodeBlock.Builder.assarr(el : Element) {
        val target = targetName(getVariable(el.name), true)
        add("«%N[", target).addArgs(el["args"]).add("] = ")
        addExpr(el["expr"]).add("\n»")
    }

    fun CodeBlock.Builder._goto(el : Element) {
        add("/* goto */)")
    }



    fun CodeBlock.Builder.addCodeBlock(code : Iterable<Element>) : CodeBlock.Builder {

        val count = assigned.size

        addCode(code)

        var drop = assigned.subList(count, assigned.size)

        if(drop.isNotEmpty())
            drop.clear()

        return this;
    }

    fun CodeBlock.Builder._if(el : Element) : CodeBlock.Builder {
        val elements = el.children()
        val cond = elements.removeAt(0)

        _if(cond, elements)

        return this
    }

    fun CodeBlock.Builder._if(cond : Element, elements : MutableList<Element> ) : CodeBlock.Builder {

        add("if(")
        addExpr(cond.children())
        beginControlFlow(")")

        while(elements.isNotEmpty()) {
            val e = elements.removeAt(0)
            val name = e.tagName
            when(name) {
                "then" -> addCodeBlock(e.children())
                "else" -> nextControlFlow(" else ").addCodeBlock(e.children())
                "elif" -> unindent().add("} else if(").addExpr(e.children()).beginControlFlow(")")
            }
        }

        endControlFlow()

        return this
    }
    
    fun CodeBlock.Builder._do(el : Element) : CodeBlock.Builder {
        val elements = el.children()
        val cond = elements.removeAt(0)

        val type = cond.tagName

        when(type) {
            "while" -> _while(cond, elements)
            "for" -> _for(cond, elements)
            else -> throw RuntimeException(type)
        }

        return this
    }

    fun CodeBlock.Builder._for(cond : Element, elements : MutableList<Element>) : CodeBlock.Builder {

        var args = cond.all("arg")

        add("for(%N in ", cond.name).addExpr(args[0]).add("..").addExpr(args[1])
        if(args.size>2)
            add(" step ").addExpr(args[2])

        beginControlFlow(")")
        addCodeBlock(elements)
        endControlFlow()

        return this
    }

    fun CodeBlock.Builder._while(cond : Element, elements : MutableList<Element>) : CodeBlock.Builder {
        add("while (").addExpr(cond)
        beginControlFlow(")")
        addCodeBlock(elements)
        endControlFlow()
        return this
    }

    fun CodeBlock.Builder.cycle() {

    }

    fun CodeBlock.Builder._exit() {

    }

    fun CodeBlock.Builder._return() : CodeBlock.Builder {

        if(retval!=null) {
            add("return %N\n", retval.name)
        } else
            add("return\n")

        return this;
    }

}
