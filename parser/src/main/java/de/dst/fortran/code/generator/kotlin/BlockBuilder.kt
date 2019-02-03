package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.CodeBlock
import de.dst.fortran.code.Local
import de.dst.fortran.code.Variable
import org.w3c.dom.Element

open class BlockBuilder(method: MethodGenerator) : ExpressionBuilder(method) {

    open fun parent() = this
    
    var locals = mutableMapOf<String, Local>()

    fun locals(element : Element) {
        element.all("variable").forEach{
            val local = Local(it.name, it.attributes["type"])
            if(local.isAssigned) {
                if(local.isModified)
                    local.stat = Local.Stat.M
                else
                    local.stat = Local.Stat.U
            }
            local(local)
        }
    }

    fun Local.assign() {
        if(!isAssigned)
            stat = if(isModified) Local.Stat.A else Local.Stat.AM
        else
            write()
    }

    open fun local(local : Local) {

        code.add("// ${local.name}: ${local.stat}\n")

        locals.put(local.name, local)
    }

    open inner class Block : BlockBuilder(method) {

        override fun parent() = this@BlockBuilder

        init {
            // copy local variables
            this@BlockBuilder.locals.values.forEach{it->locals.put(it.name, it)}
        }

        override fun local(local : Local) {
            code.add("// ${local.name}: ${local.stat}\n")

            // lookup external context
            val ext = locals.get(local.name)

            if(ext!=null) {
                if(ext.isExpected) {
                    ext.stat = Local.Stat.A;
                    if (!ext.isModified) // always a var
                        ext.write()

                    // declare as var
                    parent().declVariable(getVariable(local.name))
                }
                locals.put(ext.name, ext)
                code.add("// ${ext.name} -> ${ext.stat}\n")

            } else
                locals.put(local.name, local)
        }
    }

    fun isAssigned(variable : Variable) = locals.get(variable.name)?.isAssigned?:false
    fun isModified(variable : Variable) = locals.get(variable.name)?.isModified?:false

    fun assignLocal(name : String) : Local{
        var local = locals.get(name)
        if(local==null)
            local = Local(name, Local.Stat.A)
        else
            local.assign()

        return local
    }

    fun isDefined(variable : Variable) = !variable.isLocal() || isAssigned(variable)

    fun declVariable(v : Variable) {
        code.add("«var %N = ", v.name)
        code.add(v.initialize(v.asKlass()))
        code.add("\n»")
        assignLocal(v.name).write()
    }

    override fun getVariable(name: String): Variable {
        return super.getVariable(name)
    }

    fun assvar(el : Element) {
        val variable = getVariable(el.name);
        val target = targetName(variable, false)

        val def = when {
            isDefined(variable) -> "«%N = "
            isModified(variable) -> "«var %N = "
            else -> "«val %N = "
        }

        assignLocal(variable.name)

        code.add(def, target)
        addExpr(el.children())
        code.add("\n»")
    }

    fun addCode(elements : Iterable<Element>) : BlockBuilder {

        for (child in elements) {
            addCode(child)
        }

        return this
    }

    fun addCodeBlock(elements : Iterable<Element>) {

        code.add(Block()
                .addCode(elements)
                .build())
    }

    open fun addCode(elem : Element) {
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

    fun addIf(el : Element) {

        val block = object : Block() {

            override fun addCode(elem : Element) {
                val tag = elem.getTagName()
                when(tag) {
                    "locals" -> locals(elem)
                    "cond" -> {
                        addExpr(elem.children())
                        code.beginControlFlow(")")
                    }
                    "then" -> addCodeBlock(elem.children())
                    "else" -> {
                        code.nextControlFlow(" else ")
                        addCodeBlock(elem.children())
                    }
                    "elif" -> {
                        code.add("⇤} else if(")
                        addExpr(elem.children())
                        code.beginControlFlow(")")
                    }
                    else -> unknown(elem)
                }
            }
        }

        block.code.add("if(")
        block.addCode(el.children())
        block.code.endControlFlow()

        code.add(block.build())
    }

    fun addDo(el : Element)  {

        val block = object : Block() {

            override fun addCode(elem : Element) {
                val tag = elem.getTagName()
                when(tag) {
                    "locals" -> locals(elem)
                    "while" -> addWhile(elem)
                    "for" -> addFor(elem)
                    else -> super.addCode(elem);
                }
            }

            var seen = false;

            fun addFor(expr : Element) {

                if(seen)
                    throw RuntimeException("unexpected $expr.name")

                var args = expr.all("arg")

                code.add("for(%N in ", expr.name)
                addExpr(args[0])
                code.add("..")
                addExpr(args[1])

                if(args.size>2) {
                    code.add(" step ")
                    addExpr(args[2])
                }

                code.beginControlFlow(")")

                seen = true
            }

            fun addWhile(expr : Element) {
                if(seen)
                     throw RuntimeException("unexpected $expr.name")

                code.add("while (")
                addExpr(expr)
                code.beginControlFlow(")")

                seen = true
            }

            override fun build(): CodeBlock {
                if(!seen)
                    throw RuntimeException("empty do loop")

                code.endControlFlow()
                return super.build()
            }
        }

        block.addCode(el.children())
        
        code.add(block.build())
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