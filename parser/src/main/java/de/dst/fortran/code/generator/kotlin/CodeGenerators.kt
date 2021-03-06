package de.dst.fortran.code.generator.kotlin

import de.dst.fortran.XmlWriter
import de.dst.fortran.code.Analyzer
import de.dst.fortran.code.Code
import de.dst.fortran.code.Common
import java.io.File

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

    fun asProperty(block : Common) = commons.asProperty(block)
    fun asProperty(block : Code) = units.asProperty(block)

    fun generate() {
        commons.generate()
        units.generate()
    }
}

fun main(args: Array<String>) {

    val document = Analyzer.parse(*args)
    try {
        val analyzer = Analyzer.analyze(document)

        val root = File("irt3d/src/main/kotlin")
        val generator = CodeGenerators(root, "de.irt.kfor.irt3d")

        analyzer.commons().forEach(generator.commons::add)
        analyzer.units().forEach(generator.units::add)

        generator.generate()
    } finally {
        XmlWriter.writeDocument(document, File("parsed.xml"))
    }
}