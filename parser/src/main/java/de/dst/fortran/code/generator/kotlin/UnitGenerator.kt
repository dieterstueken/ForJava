package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.*
import de.dst.fortran.code.Analyzer
import de.dst.fortran.code.CodeElement
import de.dst.fortran.code.Context
import de.dst.fortran.code.Variable
import de.irt.kfor.Fortran
import de.irt.kfor.Units
import org.w3c.dom.Element
import org.w3c.dom.NamedNodeMap

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.01.19
 * Time: 18:17
 */

class UnitGenerator(generators : CodeGenerators, className : ClassName, val block : CodeElement)
    : CodeGenerator(generators, className, block.name) {
    override val initialize = "function(%T::class)"

    var lineNumber = "";

    val code = block.code()

    companion object {
        fun create(generators: CodeGenerators, element: CodeElement): UnitGenerator {
            val className = ClassName(generators.packageRoot + '.' + element.code().path, element.camelName())
            return UnitGenerator(generators, className, element)
        }

        fun CodeElement.camelName(): String {

            var name = this.code().name;

            var c = name[0];
            if (c.isUpperCase())
                return name;

            return c.toUpperCase() + name.substring(1)
        }

        fun blocks(generators: CodeGenerators): CodeBlocks<CodeElement> {
            return object : CodeBlocks<CodeElement>(generators) {
                override fun generate(block: CodeElement) = UnitGenerator.create(generators, block);
            }
        }
    }

    operator fun Element.get(name : String) = Analyzer.childElement(this, name)

    fun Element.children() : List<Element> = Analyzer.childElements(this)

    fun Element?.all(name : String) = Analyzer.childElements(this, name)

    // node.attributes[name]
    operator fun NamedNodeMap?.get(name : String) = this?.getNamedItem(name)?.nodeValue ?: ""

    override fun generate() {
        try {
            generateUnit();
        } catch(error : Throwable) {
            throw RuntimeException("error building ${code.name} at line $lineNumber  ", error);
        }
    }

    fun generateUnit() {

        val properties = code.commons.asSequence().map(generators::asProperty).asIterable()

        val units = code.blocks.asSequence().map(generators::asProperty).asIterable()

        val members = code.variables.asSequence()
                .filter(Variable::isMember)
                .map { it.asProperty() }
                .asIterable()

        // instrinsic function
        val functions = block.element()
                .get("functions")
                .all("function")
                .map(::asFunction)
                .asIterable()

        FileSpec.builder(className.packageName, className.simpleName)
                .addType(TypeSpec.classBuilder(className.simpleName)
                        .superclass(Fortran::class)
                        .primaryConstructor(FunSpec.constructorBuilder()
                                .addParameter("units", Units::class)
                                .build())
                        .addSuperclassConstructorParameter(CodeBlock.of("units"))
                        .addProperties(properties)
                        .addProperties(units)
                        .addProperties(members)
                        .addFunctions(functions)
                        .build()
                )
                .build()
                .writeTo(generators.root)
    }

    fun asFunction(fel : Element) : FunSpec {
        val name = fel.getAttribute("name")
        val variable = code.variables.find(name)
        if (variable == null || variable.context !== Context.FUNCTION)
            throw IllegalArgumentException("undefined function: $name")

        val type = variable.type().asKlass()

        val assarr = fel["assarr"]

        val parameters = assarr["args"].all("arg").asSequence()
                .flatMap{it.all("var").asSequence()}
                .map{it.attributes["name"]}
                .map{it->Variable(it).asParameter()}
                .asIterable()

        val body = CodeBlock.builder()
                .add("« return ")
                .expr(assarr["expr"])
                .add("\n»")
                .build()

        return FunSpec.builder(name)
                .addParameters(parameters)
                .returns(type)
                .addCode(body)
                .build()
    }


    fun CodeBlock.Builder.exprs(exprs : Element) : CodeBlock.Builder {
        return exprs(exprs.children())
    }

    fun CodeBlock.Builder.exprs(exprs : Iterable<Element>) : CodeBlock.Builder {
        exprs.forEach{expr(it)}
        return this
    }

    fun CodeBlock.Builder.expr(expr : Element) : CodeBlock.Builder {

        when(expr.getTagName()) {
            "F" -> lineNumber = expr.getAttribute("line");

            "c" -> comment(expr)

            "b" -> braced(expr)

            "expr","while" -> exprs(expr)

            "arg" -> arg(expr)
            "var" -> variable(expr)
            "val" -> value(expr)
            "fun" -> call(expr)
            "string" -> addString(expr)

            "add" -> add("+")
            "sub" -> add("-")
            "neg" -> add("-")
            "mul" -> add("*")
            "div" -> add("/")
            "pow" -> add(" pow ")

            "eq" -> add("==")
            "ne" -> add("!=")
            "le" -> add("<=")
            "lt" -> add("<")
            "ge" -> add(">=")
            "gt" -> add(">")

            "and" -> add(" && ")
            "or" -> add(" || ")
            "f"  -> add("\n")

            else -> unknown(expr)
        }

        return this
    }

    fun CodeBlock.Builder.arg(expr : Element) : CodeBlock.Builder {
        // ? assigned function argument
        //val assigned = "true".equals(expr.getAttribute("returned"))
        return exprs(expr)
    }

    fun CodeBlock.Builder.variable(expr : Element) : CodeBlock.Builder {
        val name = expr.getAttribute("name")
        // todo: arguments must have precedence
        val target = code.variables.find(name)?.targetName() ?: name
        add(target)
        return this
    }

    fun CodeBlock.Builder.call(expr : Element) : CodeBlock.Builder {

        var name = expr.getAttribute("name")

        // conversions
        when(name) {
            "float" -> name = "_float"
            "int" -> name = "_int"
            "cmplx" -> name = "_cpx"
        }

        var args = expr.all("arg")

        add("${name}(")
        var sep = ""

        for (arg in args) {
            add(sep)
            expr(arg)
            sep = ", "
        }

        add(")")

        return this;
    }

    /**
     * parse a constant value
     */
    fun CodeBlock.Builder.value(expr : Element) : CodeBlock.Builder {
        var value = expr.textContent

        when(value) {
            "true" -> add("%L", true)
            "false" -> add("%L", false)
            else -> {
                if(value.contains('.')) {
                    if(value.contains('d')) {
                        value = value.replace('d', 'E')
                        add("%L", value.toDouble())
                    } else
                        add("%L", value.toFloat())
                } else
                    add("%L", value.toInt())
            }
        }

        return this;
    }

    fun CodeBlock.Builder.addString(expr : Element) : CodeBlock.Builder {
        val text = expr.textContent
        if(text.length==1)
            add("%L", text[0])
        else
            add("%S", text)

        return this;
    }

    fun CodeBlock.Builder.comment(expr : Element) : CodeBlock.Builder {
        add("//")
        add(expr.getTextContent())
        add("\n");
        return this;
    }

    fun CodeBlock.Builder.braced(expr : Element) : CodeBlock.Builder {
        add("(")
        exprs(expr)
        add(")")
        return this
    }

    fun CodeBlock.Builder.unknown(expr : Element) : CodeBlock.Builder {
        add("// ?? ")
        add(expr.getTextContent())
        add("\n");
        return this;
    }
}

