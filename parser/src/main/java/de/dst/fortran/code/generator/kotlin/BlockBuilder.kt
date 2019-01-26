package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.CodeBlock
import de.dst.fortran.code.Local
import de.dst.fortran.code.Variable
import org.w3c.dom.Element

open class BlockBuilder(method: MethodGenerator) : ExpressionBuilder(method) {

    var locals = mutableMapOf<String, Local>()

    fun locals(element : Element) {
        element.all("variables").forEach{
            val local = Local(it.name, it.attributes["type"])
            locals.put(local.name, local)
        }
    }

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

    fun isDefined(variable : Variable) = !variable.isLocal() || isAssigned(variable)

    fun declVariable(v : Variable) {
        code.add("«var %N = ", v.name)
        code.add(v.initialize(v.asKlass()))
        code.add("\n»")
        assign(v)
    }

    fun assvar(el : Element) {
        val variable = getVariable(el.name);
        val target = targetName(variable, false)

        val def = when {
            isDefined(variable) -> "«%N = "
            variable.isModified() -> "«var %N = "
            else -> "«val %N = "
        }

        code.add(def, target)
        addExpr(el.children())
        code.add("\n»")
    }
    
    fun body(elem : Element) {
         addCode(elem.children())
    }

    fun addCode(elements : Iterable<Element>) {

        for (child in elements) {
            addCode(child)
        }
    }


    fun addCodeBlock(elements : Iterable<Element>) {

        val block = object : BlockBuilder(method) {

            override fun build() : CodeBlock {
                addCode(elements)
                return super.build()
            }
        }

        code.add(block.build())
    }

    fun addCode(elem : Element) {
        val tag = elem.getTagName()
        when(tag) {
            "locals" -> locals(elem)
            "F" -> codeLine(elem)
            "f"  -> contLine(elem)
            "c" -> comment(elem)
            "C" -> commentLine(elem)

            "assvar" -> assvar(elem)

            "assarr" -> assarr(elem)

            "call" -> call(elem)

            "goto" -> addGoto(elem)

            "if" -> addIf(elem)
            "do"->  addDo(elem)

            "cycle" -> addCycle()

            "exit" -> addExit()

            "return" -> addReturn()

            else -> unknown(elem)
        }
    }

    fun call(expr : Element) {
        var name : String = expr.name
        code.add("«%N(", name)
        addArgs(expr)
        code.add(")\n»")
    }

    fun assarr(el : Element) {
        val target = targetName(getVariable(el.name), true)
        code.add("«%N[", target)
        addArgs(el["args"])
        code.add("] = ")
        addExpr(el["expr"])
        code.add("\n»")
    }

    fun addGoto(el : Element) {
        code.add("/* goto */)")
    }

    fun addIf(el : Element){
        val elements = el.children()
        val cond = elements.removeAt(0)

        addIf(cond, elements)

    }

    fun addIf(cond : Element, elements : MutableList<Element> ) {

        code.add("if(")
        addExpr(cond.children())
        code.beginControlFlow(")")

        while(elements.isNotEmpty()) {
            val e = elements.removeAt(0)
            val name = e.tagName
            when(name) {
                "then" -> addCodeBlock(e.children())
                "else" -> {
                    code.nextControlFlow(" else ")
                    addCodeBlock(e.children())
                }
                "elif" -> {
                    code.add("⇤} else if(")
                    addExpr(e.children())
                    code.beginControlFlow(")")
                }
            }
        }

        code.endControlFlow()
    }

    fun addDo(el : Element)  {
        val elements = el.children()
        val cond = elements.removeAt(0)

        val type = cond.tagName

        when(type) {
            "while" -> addWhile(cond, elements)
            "for" -> addFor(cond, elements)
            else -> throw RuntimeException(type)
        }
    }

    fun addFor(cond : Element, elements : MutableList<Element>) {

        var args = cond.all("arg")

        code.add("for(%N in ", cond.name)
        addExpr(args[0])
        code.add("..")
        addExpr(args[1])

        if(args.size>2) {
            code.add(" step ")
            addExpr(args[2])
        }

        code.beginControlFlow(")")
        addCodeBlock(elements)
        code.endControlFlow()
    }

    fun addWhile(cond : Element, elements : MutableList<Element>) {
        code.add("while (")
        addExpr(cond)
        code.beginControlFlow(")")
        addCodeBlock(elements)
        code.endControlFlow()
    }

    fun addCycle() {

    }

    fun addExit() {

    }

    fun addReturn() {
        val retval = method.generator.retval
        if(retval==null)
            code.addStatement("return")
        else
            code.addStatement("return %N", retval.name)
    }
}