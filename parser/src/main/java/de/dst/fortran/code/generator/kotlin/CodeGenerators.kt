package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.*
import de.dst.fortran.code.*
import java.io.File
import kotlin.reflect.KClass

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  10.01.2019 09:47
 * modified by: $Author$
 * modified on: $Date$
 */

class CodeGenerators(val root : File, val packageRoot : String) {

    val types : Types = Types()

    val commons = mutableMapOf<String, TypeName>()

    val units = mutableMapOf<String, CodeGenerator>()

    fun generateCommon(common : Common) {
        val name = common.getName().toUpperCase();

        FileSpec.builder(packageRoot + ".common", name)
                .addCommon(common)
                .build()
                .writeTo(root);
    }

    fun FileSpec.Builder.addCommon(common : Common) : FileSpec.Builder {
        val name = common.getName().toUpperCase()
        val spec = TypeSpec.classBuilder(name)
                .addProperties(common.properties())
                .build()

        val other = commons.put(common.getName(), ClassName(packageName, name))
        if(other!=null)
            throw RuntimeException("duplicate common block: $name");

        return addType(spec)
    }

    fun Common.properties() = this.members().asIterable().map{it.asProperty()}

    fun Variable.asProperty() : PropertySpec {
        val type = this.getKlass()
        return PropertySpec.builder(this.getName(), type)
                .initializer(this.initialize(type))
                .build()
    }

    fun Variable.getKlass() : KClass<*> {
        return this.type().getKlass()
    }

    fun Variable.initialize(type : KClass<*>) = when(type) {
            Byte::class, Short::class, Int::class, Long::class -> CodeBlock.of("0")
            Float::class -> CodeBlock.of("0.0F")
            Double::class -> CodeBlock.of("0.0")
            String::class -> CodeBlock.of("")
            Boolean::class -> CodeBlock.of("false")
            else -> {
                when(this.dim.size) {
                    1 -> CodeBlock.of("%T(%L)", type, this.dim[0])
                    2 -> CodeBlock.of("%T(%L, %L)", type, this.dim[0], this.dim[1])
                    3 -> CodeBlock.of("%T(%L, %L, %L)", type, this.dim[0], this.dim[1], this.dim[2])
                    else -> CodeBlock.of("%T()", type)
                }
            }
    }

    fun TypeDef.getKlass() : KClass<*> {
        return types.get(this)
    }

    ///////////////////////////////////////////////////////////

    fun generateUnit(element : BlockElement) {
        val name = element.block().name;
        val generator = UnitGenerator.create(this, element);
        units.put(name, generator);
    }
}

fun main(args: Array<String>) {

    val document = Analyzer.parse(*args)
    val analyzer = Analyzer.analyze(document)

    val root = File("irt3d/src/main/kotlin")
    val generator = CodeGenerators(root, "de.irt.jfor.irt3d")

    for (common in analyzer.commons()) {
        generator.generateCommon(common);
    }

    for(unit in analyzer.units()) {
        generator.generateUnit(unit)
    }
}