package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.CodeBlock
import de.dst.fortran.code.Locals
import de.dst.fortran.code.VStat
import de.dst.fortran.code.Variable
import org.w3c.dom.Element

open class BlockBuilder(method: MethodGenerator, bel : Element) : ExpressionBuilder(method) {

    // forward definition saved by previous scan
    var forwards = Locals()

    // actual locals
    var locals = Locals()

    init {
        bel["locals"].all("var").forEach{
            val name = it.name;
            val stat = VStat.of(it.attributes["type"])
            forwards.put(name, stat)
        }
    }

    fun Locals.isDefined(name : String) = get(name)!=null
    fun Locals.isAssigned(name : String) = get(name)?.isAssigned?:false
    fun Locals.isModified(name : String) = get(name)?.isModified?:false
    fun Locals.isExpected(name : String) = get(name)?.isExpected?:false

    fun locals(element : Element) {
        // done
    }

    open inner class Block(bel : Element) : BlockBuilder(method, bel) {

        init {
            // copy context
            forwards.names.forEach(this@BlockBuilder::advertizeVariable)
            locals.putAll(this@BlockBuilder.locals)
        }

        override fun build(): CodeBlock {
            locals.applyTo(this@BlockBuilder.locals)
            return super.build()
        }
    }

    fun addCodeBlock(bel : Element) {

        code.add(Block(bel)
                .addCode(bel.children())
                .build())
    }

    fun advertizeVariable(name : String) {
        if (forwards.isExpected(name))
            if(!locals.isDefined(name)) {
                val v = getVariable(name)
                declVariable(v)
            }
    }

    fun declVariable(v : Variable) {
        code.add("«var %N = ", v.name)
        code.add(v.initialize(v.asKlass()))
        code.add("  // generated\n»")
        locals.write(v.name)
    }

    fun assvar(el : Element) {
        val variable = getVariable(el.name);
        val target = targetName(variable, false)

        val def = when {
            !variable.isLocal() -> "«%N = "
            locals.isDefined(variable.name) -> "«%N = "
            forwards.isModified(variable.name) -> "«var %N = "
            else -> "«val %N = "
        }

        code.add(def, target)
        addExpr(el.children())
        code.add("\n»")

        locals.write(variable.name)
    }

    fun addCode(elements : Iterable<Element>) : BlockBuilder {

        for (child in elements) {
            addCode(child)
        }

        return this
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

        val block = object : Block(el) {

            override fun addCode(elem : Element) {
                val tag = elem.getTagName()
                when(tag) {
                    "locals" -> locals(elem)
                    "cond" -> {
                        addExpr(elem.children())
                        code.beginControlFlow(")")
                    }
                    "then" -> addCodeBlock(elem)
                    "else" -> {
                        code.nextControlFlow(" else ")
                        addCodeBlock(elem)
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

        val block = object : Block(el) {

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