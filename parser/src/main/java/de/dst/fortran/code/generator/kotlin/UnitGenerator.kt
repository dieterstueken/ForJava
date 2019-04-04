package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import de.dst.fortran.code.CodeElement
import de.dst.fortran.code.TypeDef
import de.dst.fortran.code.Variable
import de.irt.kfor.Fortran
import de.irt.kfor.Units
import org.w3c.dom.Element
import kotlin.reflect.KClass

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.01.19
 * Time: 18:17
 */

fun CodeElement.camelName(): String {

    var name = this.code().name;

    var c = name[0];
    if (c.isUpperCase())
        return name;

    return c.toUpperCase() + name.substring(1)
}

class UnitGenerator(generators : CodeGenerators, override val block : CodeElement, className : ClassName)
    : ClassGenerator(generators, "function", className), Generator {

    companion object {
        fun create(generators: CodeGenerators, element: CodeElement): UnitGenerator {
            val className = ClassName(generators.packageRoot + '.' + element.code().path, element.camelName())
            return UnitGenerator(generators, element, className)
        }

        fun blocks(generators: CodeGenerators): CodeBlocks<CodeElement> {
            return object : CodeBlocks<CodeElement>(generators) {
                override fun generate(block: CodeElement) = UnitGenerator.create(generators, block);
            }
        }
    }
    override val generator = this

    var lineNumber : Int = block.line.toInt()

    fun setLineNumber(line : String?) {
        lineNumber = if(line==null || line.isEmpty()) 0 else line.toInt()
        debug(block.name, lineNumber)
    }

    fun debug(name : String, line : Int) {

    }

    override fun getKlass(type : TypeDef?) : KClass<*> = generators.types.get(type)

    val type : KClass<*> = getKlass(code.getReturnType().primitive())

    override fun targetName(variable : Variable, asReference: Boolean) : String {
        var target = ""

        if(variable.context!=null) {
            if(variable.context!=code)
                target = "${variable.context.name}."
        }

        target += "%N"

        if(!asReference && variable.isProperty())
            target += ".v"

        return target
    }

    // variable
    override val retval = if(Unit::class==type) null else
        Variable(block.name)
                .type(code.getReturnType())
                .prop(Variable.Prop.ASSIGNED)
                .prop(Variable.Prop.RETURNED)

    val functions = mutableMapOf<String, LocalFunction>()


    override fun getVariable(name: String) : Variable {

        if(name == block.name)
            return retval!!

        val v = code.variables.find(name)!!

        if(v.ref==null)
            return v

        if(v.isIndex())
            return v.ref

        if(!v.ref.isIndex())
            return v.ref;

        return v;
    }

    fun Variable.isMember(): Boolean = context == null && !isPrimitive() && name != code.name

    override fun toString() : String = "${code.path}.${code.name}:${lineNumber}"

    override fun generate() {
        try {
            super.generate();
        } catch (error: Throwable) {
            throw RuntimeException("error building ${this}", error)
        }
    }

    override fun TypeSpec.Builder.generate(): TypeSpec.Builder {

        val name = block.element.name

        block.element["decl"].all("C")
                .forEach{e -> addKdoc(e.textContent.replace("%", "%%") + "\n")}

        superclass(Fortran::class)

        primaryConstructor(FunSpec.constructorBuilder()
                .addParameter("units", Units::class)
                .build())

        addSuperclassConstructorParameter(CodeBlock.of("units"))

        for (common in code.commons) {
            addProperty(generators.asProperty(common))
        }

        for (unit in code.blocks) {
            addProperty(generators.asProperty(unit))
        }

        for (variable in code.variables) {
            if (variable.isMember())
                addProperty(variable.asProperty())
        }

        for (func in block.element["functions"].all("function")) {
            addFunction(localFunction(func))
        }

        for(data in block.element["decl"].all("data")) {
            addInitializerBlock(dataBlock(data));
        }

        addFunction(mainFunction())

        return this;
    }

    private fun dataBlock(data: Element) : CodeBlock {

        val code = CodeBlock.builder()

        val names = data.all("name")
        var values = data["values"].children()

        for(name in names) {
            val variable = getVariable(name.name)
            val target = targetName(variable, false)

            code.add("«val $target.init(", target)
            //when(variable.dim.size) {
            //    0 ->
            //}
            code.add(")\n»")
        }

        return code.build();
    }

    fun localFunction(func: Element) : FunSpec {
        val lf = LocalFunction(this, func)
        functions.put(lf.element.name, lf)
        return lf.build()
    }

    fun mainFunction() = MainFunction(this).build()
}

