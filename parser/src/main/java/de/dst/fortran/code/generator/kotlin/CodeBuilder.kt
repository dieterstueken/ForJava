package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.CodeBlock
import de.dst.fortran.code.Variable
import org.w3c.dom.Element

class CodeBuilder(method: MethodGenerator) : ExpressionBuilder(method) {

    var assigned = mutableListOf<String>()

    fun isAssigned(variable : Variable) : Boolean {

        for (name in assigned) {
            if(name==variable.name)
                return true
        }

        return false
    }

    fun assign(variable : Variable) : Boolean {
        if(isAssigned(variable))
            return false

        assigned.add(variable.name)
        return true;
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

            "if" -> addIf(elem)
            "do"->  addDo(elem)

            "cycle" -> addCycle()

            "exit" -> addExit()

            "return" -> addReturn()

            else -> unknown(elem)
        }

        return this
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

    fun CodeBlock.Builder.addIf(el : Element) : CodeBlock.Builder {
        val elements = el.children()
        val cond = elements.removeAt(0)

        addIf(cond, elements)

        return this
    }

    fun CodeBlock.Builder.addIf(cond : Element, elements : MutableList<Element> ) : CodeBlock.Builder {

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

    fun CodeBlock.Builder.addDo(el : Element) : CodeBlock.Builder {
        val elements = el.children()
        val cond = elements.removeAt(0)

        val type = cond.tagName

        when(type) {
            "while" -> addWhile(cond, elements)
            "for" -> addFor(cond, elements)
            else -> throw RuntimeException(type)
        }

        return this
    }

    fun CodeBlock.Builder.addFor(cond : Element, elements : MutableList<Element>) : CodeBlock.Builder {

        var args = cond.all("arg")

        add("for(%N in ", cond.name).addExpr(args[0]).add("..").addExpr(args[1])
        if(args.size>2)
            add(" step ").addExpr(args[2])

        beginControlFlow(")")
        addCodeBlock(elements)
        endControlFlow()

        return this
    }

    fun CodeBlock.Builder.addWhile(cond : Element, elements : MutableList<Element>) : CodeBlock.Builder {
        add("while (").addExpr(cond)
        beginControlFlow(")")
        addCodeBlock(elements)
        endControlFlow()
        return this
    }

    fun CodeBlock.Builder.addCycle() {

    }

    fun CodeBlock.Builder.addExit() {

    }

    fun CodeBlock.Builder.addReturn() : CodeBlock.Builder = method.addReturn(this)

}