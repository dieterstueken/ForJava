package de.dst.fortran.analyzer;

import com.sun.codemodel.internal.*;
import de.dst.fortran.code.Common;
import de.dst.fortran.code.Type;
import de.dst.fortran.code.Variable;

import java.io.File;
import java.io.IOException;


/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:01
 * modified by: $Author$
 * modified on: $Date$
 */
public class CodeGenerator {

    private final JCodeModel codeModel = new JCodeModel();

    private final JPackage jfor = codeModel._package("de.irt.jfor");

    private final JPackage jmodule;

    public CodeGenerator(String module) {
        jmodule = codeModel._package(module);
    }

    JDefinedClass defineClass(JPackage jp, String name) {

        char c = name.charAt(0);
        c = Character.toUpperCase(c);
        name = Character.toString(c) + name.substring(1);

        try {
            return  jp._class(name);
        } catch (JClassAlreadyExistsException e) {
            throw new RuntimeException(e);
        }
    }

    public void generate(Analyzer code) {
        generateCommons(code);
    }

    public void build(File directory) throws IOException {
        codeModel.build(directory);
    }

    public void generateCommons(Analyzer code) {

        JPackage jpkg = jmodule.subPackage("common");
        for (Common common : code.commons.values()) {
            generateCommon(common, defineClass(jpkg, common.name));
        }
    }

    void generateCommon(Common common, JDefinedClass jc) {
        jc._extends(de.irt.jfor.Common.class);
        for (Variable member : common.members) {
            Type type = member.type();
            JExpression init = type.init();
            jc.field(JMod.PUBLIC|JMod.FINAL, type.type, member.name, init);
        }
    }

}
