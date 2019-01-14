package de.dst.fortran.code.generator.kotlin

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import de.dst.fortran.code.Common

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.01.19
 * Time: 19:24
 */
class CommonGenerator (generators : CodeGenerators, className : ClassName, val common : Common)
    : CodeGenerator(generators, className) {

    companion object {
        fun create(generators : CodeGenerators, common : Common) : CommonGenerator {
            val name = common.getName().toUpperCase()
            val className = ClassName(generators.packageRoot + ".common", name)
            return CommonGenerator(generators, className, common)
        }
    }

    override fun build() {
        FileSpec.builder(className.packageName, className.simpleName)
                .addType(TypeSpec.classBuilder(className.simpleName)
                        .addProperties(properties())
                        .build())
                .build()
                .writeTo(generators.root);
    }

    fun properties() = common.members().map{
        val type = it.getKlass()
                return PropertySpec.builder(it.getName(), type)
                        .initializer(it.initialize(type))
                        .build()
    }
}