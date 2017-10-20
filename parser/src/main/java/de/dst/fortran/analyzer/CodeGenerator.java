package de.dst.fortran.analyzer;

import com.helger.jcodemodel.*;
import de.dst.fortran.code.*;
import de.irt.jfor.Ref;

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

    private final JCodeModel codeModel = new JCodeModel();

    private final JPackage jfor = codeModel._package("de.irt.jfor");

    private final JPackage jmodule;

    private Map<String, JDefinedClass> commons = new HashMap<>();
    private Map<String, JDefinedClass> units = new HashMap<>();

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

    JFieldVar defineVariable(JDefinedClass jc, Variable var) {
        Class<?> type = var.type();

        IJExpression expr = null;

        if(Ref.class.isAssignableFrom(type)) {
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

        return jc.field(JMod.PUBLIC|JMod.FINAL, type, var.name, expr);
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
            JPackage jpkg = jmodule.subPackage(analyzer.block.path);
            JDefinedClass jc = defineClass(jpkg, analyzer.block.name);
            jc._extends(de.irt.jfor.Unit.class);

            units.put(analyzer.block.name, jc);
        }

        for (BlockAnalyzer analyzer : code.analyzers.values()) {
            JDefinedClass jc = units.get(analyzer.block.name);
            generateUnit(analyzer, jc);
        }

    }

    private JDefinedClass generateUnit(BlockAnalyzer analyzer, JDefinedClass jc) {
        
        JDocComment comment = null;

        for (Common common : analyzer.block.commons) {
            JDefinedClass jcommon = commons.get(common.name);
            JFieldVar jvar = jc.field(JMod.PUBLIC|JMod.FINAL,
                jcommon, common.name,
                    JExpr.invoke("common").arg(JExpr.dotclass(jcommon)));
            if(comment==null)
                comment = jvar.javadoc();
        }

        if(comment!=null) {
            comment.setSingleLineMode(true);
            comment.add("\n");
            comment.add("commons");
            comment = null;
        }

        for (Block block : analyzer.block.blocks) {
            String name = block.name;
            JDefinedClass junit = units.get(name);
            JFieldVar jvar = jc.field(JMod.PUBLIC|JMod.FINAL,
                    junit, name,
                    JExpr.invoke("unit").arg(JExpr.dotclass(junit)));
            if(comment==null)
                comment = jvar.javadoc();

        }

        if(comment!=null) {
            comment.setSingleLineMode(true);
            comment.add("\n");
            comment.add("units");
            comment = null;
        }

        for (Variable var : analyzer.block.variables) {
            if(var.context==null) {
                JFieldVar jvar = defineVariable(jc, var);
                if (comment == null)
                    comment = jvar.javadoc();
            }
        }

        if(comment!=null) {
            comment.setSingleLineMode(true);
            comment.add("\n");
            comment.add("variables");
            comment = null;
        }

        JMethod call = jc.method(JMod.PUBLIC, analyzer.block.type(), "call");
        for (Variable arg : analyzer.block.arguments) {
            call.param(JMod.FINAL, arg.type(), arg.name);
        }
        
        return jc;
    }
}
