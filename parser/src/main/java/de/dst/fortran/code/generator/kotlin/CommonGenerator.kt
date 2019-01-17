package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import de.dst.fortran.code.Common

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.01.19
 * Time: 19:24
 */
class CommonGenerator(generators : CodeGenerators, val common : Common, className : ClassName)
    : ClassGenerator(generators, "common", className) {

    companion object {
        fun create(generators : CodeGenerators, common : Common) : CommonGenerator {
            val name = common.name.toUpperCase()
            val className = ClassName(generators.packageRoot + ".common", name)
            return CommonGenerator(generators, common, className)
        }

        fun blocks(generators : CodeGenerators) : CodeBlocks<Common> {
            return object: CodeBlocks<Common>(generators) {
                override fun generate(block: Common) = create(generators, block);
            }
        }
    }

    override fun TypeSpec.Builder.generate() : TypeSpec.Builder {

        addSuperinterface(de.irt.kfor.Common::class)

        for (member in common.members()) {
            addProperty(member.asProperty())
        }

        return this
    }

}