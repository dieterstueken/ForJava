package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.CodeBlock
import de.dst.fortran.code.Locals
import de.dst.fortran.code.Type
import de.dst.fortran.code.VStat
import de.dst.fortran.code.Variable
import org.w3c.dom.Element

open class BlockBuilder(method: Generator, bel : Element) : CodeBuilder(method) {

    fun CodeBlock.Builder.addExprs(el : Element) : CodeBlock.Builder {
        val expr = expr()
        expr.addExprs(el)
        return this.add(expr.build())
    }

    fun addExprs(el : Element) : Type {
        val expr = expr()
        val type = expr.addExprs(el)
        code.add(expr.build())
        return type
    }

    fun CodeBlock.Builder.addArgs(el : Element) : CodeBlock.Builder {
        val expr = expr()
        expr.addArgs(el)
        return this.add(expr.build())
    }

    fun CodeBlock.Builder.addArgs(args : List<Element>, sep : String = ", ") : CodeBlock.Builder {
        val expr = expr()
        expr.addArgs(args, sep)
        return this.add(expr.build())
    }

    // forward definition saved by previous scan
    var forwards = Locals()

    // actual locals
    var locals = Locals()

    init {
        bel["locals"].all("var").forEach{
            val name = it.name;
            val stat = VStat.of(it.attributes["type"])

            // may have changed scope
            if(isLocal(name))
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
        val type = if(v.isInt()) Int::class else Double::class
        code.add("«var %N = ", v.name)
        code.add(v.initialize(v.asKlass()))
        code.add("  // generated\n»")
        locals.write(v.name)
    }

    fun CodeBlock.Builder.addExprs(el : Element, type : Type) : CodeBlock.Builder {
        val expr = expr()
        val xtype = expr.addExprs(el)

        val trail = when {
            type.isInt() && (xtype==null || !xtype.isInt())
            -> ".toInt()"

            type.isReal() && (xtype==null || !xtype.isReal())
            -> ".toDouble()"

            else -> ""
        }

        val build = expr.build()

        if(trail.isNotEmpty()) {
            val tag  = (el.firstChild as? Element)?.tagName
            when(tag) {
                "sum", "prod" -> add("(").add(build).add(")").add(trail)
                else -> add(build).add(trail)
            }
        } else
            this.add(build)

        return this
    }

    fun assVar(el : Element) {
        val variable = getVariable(el.name)
        var name = variable.name
        val target = targetName(variable, false)

        when {
            !variable.isLocal() -> code.add("«$target = ", name)
            locals.isDefined(variable.name) -> code.add("«$target = ", name)
            forwards.isModified(variable.name) -> code.add("«var $target = ", name)
            else -> code.add("«val $target = ", name)
        }

        code.addExprs(el, variable.typeDef().type)
            .add("\n»")

        locals.write(variable.name)
    }

    fun assArr(el : Element) {
        val variable = getVariable(el.name)
        val target = targetName(variable, true)
        val type = variable.type
        code.add("«$target[", variable.name)
                .addArgs(el["args"])
                .add("] = ")
                .addExprs(el["expr"], type)
                .add("\n»")
    }

    fun alloc(el : Element) {
        val variable = getVariable(el.name)
        val target = targetName(variable, true)
        code.add("«$target = $target.allocate(", variable.name, variable.name)
                .addArgs(el["args"])
                .add(")\n»")
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

            "assvar" -> assVar(elem)
            "assarr" -> assArr(elem)
            "allocate" -> alloc(elem)
            "deallocate" -> code.add("// deallocate ${elem.name}\\n")

            "call" -> addCall(elem)
            "print" -> addPrint(elem)
            "write" -> addWrite(elem)

            "goto" -> addGoto(elem)

            "if" -> addIf(elem)
            "do"->  addDo(elem)

            "cycle" -> addCycle()
            "exit" -> addExit()

            "return" -> addReturn()


            else -> unknown(elem)
        }
    }

    fun addCall(call : Element) {

        val block = object : Block(call) {
            override fun addCode(elem : Element) {
                var name : String = call.name

                code.add("«%N(", name)
                        .add(expr().addfArgs(elem).build())
                        .add(")\n»")
            }
        }
        block.addCode(call)

        code.add(block.build())
    }


    fun addWrite(args : Element) {
        code.add("«// write(")

        //val expr = expr();
        //expr.addArgs(args.all(Predicate {it.tagName!="io"}), ", ")
        //code.add(expr.build())
                .add(")\n»")
    }

    fun addPrint(args : Element) {
        code.add("«println(")

        val expr = expr();
        expr.addArgs(args.children(), " + ")
        code.add(expr.build())
                .add(")\n»")
    }

    fun addGoto(el : Element) {
        code.add("/* goto */)")
    }

    fun addIf(el : Element) {


        if(el.get("cond")?.get("var")?.name == "ierr") {
            if(el.get("then")?.get("deallocate")!=null) {
                code.add("// if(ierr!=0)\n")
                return;
            }
        }

        val block = object : Block(el) {

            override fun addCode(elem : Element) {
                val tag = elem.getTagName()
                when(tag) {
                    "locals" -> locals(elem)
                    "cond" -> {
                        code.addExprs(elem)
                        code.beginControlFlow(")")
                    }
                    "then" -> addCodeBlock(elem)
                    "else" -> {
                        code.nextControlFlow(" else ")
                        addCodeBlock(elem)
                    }
                    "elif" -> {
                        code.add("⇤} else if(")
                        code.addExprs(elem)
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
                code.addExprs(args[0])
                code.add("..")
                code.addExprs(args[1])

                if(args.size>2) {
                    code.add(" step ")
                    code.addExprs(args[2])
                }

                code.beginControlFlow(")")

                seen = true
            }

            fun addWhile(expr : Element) {
                if(seen)
                     throw RuntimeException("unexpected $expr.name")

                code.add("while (")
                code.addExprs(expr)
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
        val retval = method.retval
        if(retval==null)
            code.addStatement("return")
        else
            code.addStatement("return %N", retval.name)
    }
}