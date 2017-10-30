package de.dst.fortran.analyzer;

import com.helger.jcodemodel.*;
import de.dst.fortran.code.Common;
import de.dst.fortran.code.Constant;
import de.dst.fortran.code.Value;
import de.dst.fortran.code.Variable;
import de.irt.jfor.Arr;
import de.irt.jfor.Complex;
import de.irt.jfor.Ref;
import de.irt.jfor.Unit;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static de.dst.fortran.code.Value.UNDEF;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 13:01
 * modified by: $Author$
 * modified on: $Date$
 */
public class CodeGenerator {

    final JCodeModel codeModel = new JCodeModel();

    final JPackage jmodule;

    final AbstractJClass refType = codeModel.ref(Ref.class);
    final AbstractJClass arrType = codeModel.ref(Arr.class);
    final AbstractJClass unitType = codeModel.ref(Unit.class);
    final AbstractJClass cplxType = codeModel.ref(Complex.class);

    JPackage subPackage(@Nonnull String name) {
        return jmodule.subPackage(name);
    }

    Map<String, JDefinedClass> commons = new HashMap<>();
    Map<String, UnitGenerator> units = new HashMap<>();

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
        generateUnits(code);
    }

    public void build(File directory) throws IOException {
        directory.mkdirs();
        codeModel.build(directory);
    }

    public void generateCommons(Analyzer code) {

        JPackage jpkg = jmodule.subPackage("common");
        for (Common common : code.commons.values()) {
            generateCommon(common, defineClass(jpkg, common.name));
        }
    }

    JFComplex complex(IJExpression re, IJExpression im) {
        return new JFComplex(cplxType, re, im);
    }

    JFieldVar defineVariable(JDefinedClass jc, Variable var) {
        Class<?> type = var.type();

        IJExpression expr = null;
        int mod = JMod.PUBLIC;

        if(Ref.class.isAssignableFrom(type) || Complex.class.isAssignableFrom(type)) {
            JInvocation init = codeModel.ref(type).staticInvoke("of");

            if (!var.dim.isEmpty()) {
                for (Value value : var.dim) {
                    if (value instanceof Constant) {
                        int n = ((Constant) value).value.intValue();
                        init.arg(n);
                    } else if (value == UNDEF) {
                        init = null;
                        break;
                    } else {
                        throw new IllegalArgumentException(value.toString());
                    }
                }
            }
            
            mod |= JMod.FINAL;
            if(init!=null)
                expr = init;
            else
                expr = JExpr._null();
        } else
        if(String.class.isAssignableFrom(type))
            expr = JExpr._null();
        else
        if(Boolean.class.isAssignableFrom(type))
            expr = JExpr.FALSE;
        else
            expr = JExpr.lit(0);

        return jc.field(mod, type, var.name, expr);
    }

    void generateCommon(Common common, JDefinedClass jc) {
        jc._extends(de.irt.jfor.Common.class);
        commons.put(common.name, jc);
        for (Variable member : common.members) {
            defineVariable(jc, member);
        }
    }

    private void generateUnits(Analyzer code) {

        for (BlockAnalyzer analyzer : code.analyzers.values()) {
            JPackage jpkg = subPackage(analyzer.block.path);
            JDefinedClass jclass = defineClass(jpkg, analyzer.block.name);
            UnitGenerator unit = new UnitGenerator(this, analyzer, jclass);
            units.put(analyzer.block.name, unit);
        }

        units.values().forEach(UnitGenerator::define);
    }
}
