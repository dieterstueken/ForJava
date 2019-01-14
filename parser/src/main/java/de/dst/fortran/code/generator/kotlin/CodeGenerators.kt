package de.dst.fortran.code.generator.kotlin

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

    val commons = CommonGenerator.blocks(this)

    val units = UnitGenerator.blocks(this)

    fun getKlass(variable : Variable) : KClass<*> {
        return variable.type().getKlass()
    }
    
    fun TypeDef.getKlass() : KClass<*> {
        return types.get(this)
    }

    fun asProperty(block : Common) = commons.asProperty(block)
    fun asProperty(block : Code) = units.asProperty(block)

    fun generate() {
        commons.generate()
        units.generate()
    }
}

fun main(args: Array<String>) {

    val document = Analyzer.parse(*args)
    val analyzer = Analyzer.analyze(document)

    val root = File("irt3d/src/main/kotlin")
    val generator = CodeGenerators(root, "de.irt.jfor.irt3d")

    analyzer.commons().forEach(generator.commons::add)
    analyzer.units().forEach(generator.units::add)

    generator.generate();
}