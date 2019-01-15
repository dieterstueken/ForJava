package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import de.dst.fortran.code.Common

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.01.19
 * Time: 19:24
 */
class CommonGenerator(generators : CodeGenerators, className : ClassName, val common : Common)
    : CodeGenerator(generators, className, common.name) {

    override val initialize = "common(%T::class)"

    companion object {
        fun create(generators : CodeGenerators, common : Common) : CommonGenerator {
            val name = common.name.toUpperCase()
            val className = ClassName(generators.packageRoot + ".common", name)
            return CommonGenerator(generators, className, common)
        }

        fun blocks(generators : CodeGenerators) : CodeBlocks<Common> {
            return object: CodeBlocks<Common>(generators) {
                override fun generate(block: Common) = create(generators, block);
            }
        }
    }

    override fun generate() {
        FileSpec.builder(className.packageName, className.simpleName)
                .addType(TypeSpec.classBuilder(className.simpleName)
                        .addSuperinterface(de.irt.kfor.Common::class)
                        .addProperties(properties())
                        .build())
                .build()
                .writeTo(generators.root);
    }

    // variables
    fun properties() = common.members().map{it.asProperty()}
}