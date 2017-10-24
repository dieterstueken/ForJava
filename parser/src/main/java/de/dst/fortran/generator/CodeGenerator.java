package de.dst.fortran.generator;

import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JPackage;
import org.dom4j.Element;

import java.util.Map;
import java.util.TreeMap;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  23.10.2017 13:44
 * modified by: $Author$
 * modified on: $Date$
 */
public class CodeGenerator {

    final JCodeModel codeModel = new JCodeModel();

    final JPackage jmodule;

    final JPackage jcommon;

    final Map<String, CommonClass> commons = new TreeMap<>();

    public CodeGenerator(String module) {
        jmodule = codeModel._package(module);
        jcommon = jmodule.subPackage("common");
    }

    protected CommonClass common(Element ce) {
        String name = ce.attributeValue("name");
        return commons.computeIfAbsent(name, s -> createCommon(ce));
    }

    private CommonClass createCommon(Element ce) {
        String name = ce.attributeValue("name");
        JDefinedClass jc = defineClass(jcommon, name);
        return new CommonClass(this, jc, ce);
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
}
