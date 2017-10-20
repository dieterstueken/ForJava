package de.dst.fortran.analyzer;

import com.helger.jcodemodel.*;
import de.dst.fortran.code.*;

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

    void generateCommon(Common common, JDefinedClass jc) {
        jc._extends(de.irt.jfor.Common.class);
        commons.put(common.name, jc);
        for (Variable member : common.members) {
            Type type = member.type();

            JInvocation init = codeModel.ref(type.type()).staticInvoke("of");

            if(member.dim!=null)
            for (Value value : member.dim) {
                if(value instanceof Constant) {
                    int n = ((Constant) value).value.intValue();
                    init.arg(n);
                } else
                if(value == UNDEF) {
                    init = null;
                    break;
                }
                else {
                    throw new IllegalArgumentException(value.toString());
                }
            }

            IJExpression expr = init==null ? JExpr._null() : init;

            JFieldVar jvar = jc.field(JMod.PUBLIC|JMod.FINAL,
                    type.type, member.name, expr);
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
        }

        return jc;
    }
}
