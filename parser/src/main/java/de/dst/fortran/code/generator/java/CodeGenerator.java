package de.dst.fortran.code.generator.java;

import com.helger.jcodemodel.*;
import de.dst.fortran.XmlWriter;
import de.dst.fortran.code.*;
import de.irt.jfor.Arr;
import de.irt.jfor.Complex;
import de.irt.jfor.Ref;
import de.irt.jfor.Unit;
import org.w3c.dom.Document;

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

    static final Types TYPES = new Types();

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

        name = Character.toUpperCase(name.charAt(0)) + name.substring(1);

        try {
            return  jp._class(name);
        } catch (JClassAlreadyExistsException e) {
            throw new RuntimeException(e);
        }
    }

    public void generate(Analyzer code) {
        generateCommons(code.commons());
        generateUnits(code.units());
    }

    public void build(File directory) throws IOException {
        directory.mkdirs();
        codeModel.build(directory);
    }

    public void generateCommons(Iterable<? extends Common> commons) {
        JPackage jpkg = jmodule.subPackage("common");
        commons.forEach(common->{
            String name = common.getName().toUpperCase();
            generateCommon(common, defineClass(jpkg, name));
        });
    }

    Class<?> typeOf(TypeDef type) {
        return TYPES.get(type);
    }

    JFComplex complex(IJExpression re, IJExpression im) {
        return new JFComplex(cplxType, re, im);
    }

    JFieldVar defineVariable(JDefinedClass jc, Variable var, int mod) {
        Class<?> type = typeOf(var.type());

        IJExpression expr = null;

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
        commons.put(common.getName(), jc);
        common.members().forEach(member->{
            defineVariable(jc, member, JMod.PUBLIC);
        });
    }

    static String camelName(String name) {
        char c = name.charAt(0);
        if(Character.isUpperCase(c))
            return name;
        else
            return Character.toUpperCase(c) + name.substring(1);
    }

    private void generateUnits(Iterable<? extends BlockElement> blocks) {

        blocks.forEach(element -> {
            final Block block = element.block();
            JPackage jpkg = subPackage(block.path);
            String name = camelName(block.name);
            JDefinedClass jclass = defineClass(jpkg, name);
            UnitGenerator unit = new UnitGenerator(this, element, jclass);
            units.put(block.name, unit);
        });

        units.values().forEach(UnitGenerator::define);
    }

    public static void main(String ... args) throws Exception {

        Document document = Analyzer.parse(args);
        try {
            Analyzer analyzer = Analyzer.analyze(document);

            //final Set<String> intrinsics = new TreeSet<>();
            //analyzer.units().map(BlockAnalyzer::block).forEach(b->intrinsics.addAll(b.functions));
            //System.out.println("intrinsics:");
            //intrinsics.forEach(System.out::println);

            CodeGenerator generator = new CodeGenerator("de.irt.jfor.irt3d");
            generator.generate(analyzer);
            generator.build(new File("irt3d/src/main/java"));

        } finally{
            XmlWriter.writeDocument(document, new File("parsed.xml"));
        }
    }

}
