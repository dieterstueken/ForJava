package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.*
import de.dst.fortran.code.*
import de.irt.kfor.Fortran
import de.irt.kfor.Units
import java.io.File
import kotlin.reflect.KClass

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  10.01.2019 09:47
 * modified by: $Author$
 * modified on: $Date$
 */

class CodeGenerator(val root : File, val packageRoot : String) {

    val types : Types = Types()

    val files = mutableSetOf<FileSpec>()
    val commons = mutableMapOf<String, TypeSpec>()
    val units = mutableMapOf<String, TypeSpec>()

    fun generateCommon(common : Common) {
        val name = common.getName().toUpperCase();

        val file = FileSpec.builder(packageRoot + ".common", name)
                .addType(common.defineType())
                .build()

        files.add(file);
    }

    fun generateUnit(element : BlockElement) {

        val block = element.block();
        val packageName = packageRoot + '.' + block.path;

        val file = FileSpec.builder(packageName, element.block().camelName())
                .addType(element.defineType())
                .build()

        files.add(file);
    }

    fun Block.camelName() : String {

        var name = this.name;

        var c = name[0];
        if(c.isUpperCase())
            return name;

        return c.toUpperCase() + name.substring(1)
    }

    fun BlockElement.defineType() : TypeSpec {
        val name = this.block().camelName();
        val spec = TypeSpec.classBuilder(name)
                .superclass(Fortran::class)
                .primaryConstructor(FunSpec.constructorBuilder()
                        .addParameter("units", Units::class)
                        .build())
                .addSuperclassConstructorParameter(CodeBlock.of("units"))
                .addProperties(this.properties())
                .build()

        val other = units.put(name, spec);
        if(other!=null)
            throw RuntimeException("duplicate unit: $name");

        return spec;
    }

    fun BlockElement.properties() = listOf<PropertySpec>() // this.block().commons.asIterable().map{it.load()}

    //fun CommonAnalyzer.load() : PropertySpec {
    //    val spec : TypeSpec = commons.get(this.name)!!
    //    val type : TypeName
    //    return PropertySpec.builder(this.name, type)
    //            .build()
    //}


    fun Common.defineType() : TypeSpec {
        val name = this.getName().toUpperCase();
        val spec = TypeSpec.classBuilder(name)
                .addProperties(this.properties())
                .build()

        val other = commons.put(name, spec);
        if(other!=null)
            throw RuntimeException("duplicate common block: $name");

        return spec;
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


    fun generate() {
        for (file in files) {
            file.writeTo(root)
        }
    }
}

fun main(args: Array<String>) {

    val document = Analyzer.parse(*args)
    val analyzer = Analyzer.analyze(document)

    val root = File("irt3d/src/main/kotlin")
    val generator = CodeGenerator(root, "de.irt.jfor.irt3d")

    for (common in analyzer.commons()) {
        generator.generateCommon(common);
    }

    for(unit in analyzer.units()) {
        generator.generateUnit(unit)
    }

    generator.generate();
}