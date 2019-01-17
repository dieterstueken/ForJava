package de.dst.fortran.code.generator.kotlin

import de.dst.fortran.code.Context

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  14.01.2019 09:37
 * modified by: $Author$
 * modified on: $Date$
 */

abstract class CodeBlocks<T : Context>(val generators : CodeGenerators) {

    val blocks = mutableMapOf<String, ClassGenerator>()

    fun add(block : T) {
        val replaced = blocks.put(block.name, generate(block))

        if(replaced!=null)
            throw RuntimeException("duplicate fortran unit: $block.name");
    }

    protected abstract fun generate(block : T) : ClassGenerator

    fun asProperty(block : Context) = blocks.get(block.name)!!.property

    fun generate() {
        blocks.values.forEach(ClassGenerator::generate)
    }
}