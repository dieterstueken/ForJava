package de.dst.fortran.analyzer;

import com.helger.jcodemodel.*;
import de.dst.fortran.code.Block;
import de.dst.fortran.code.Common;
import de.dst.fortran.code.Variable;
import de.irt.jfor.Comment;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

import static de.dst.fortran.analyzer.Analyzer.childElements;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 21.10.17
 * Time: 15:35
 */
class UnitGenerator {
    private CodeGenerator codeGenerator;
    final BlockAnalyzer code;
    final JDefinedClass jclass;

    final Map<String, JVar> variables = new HashMap<>();

    JVar decl(JVar var) {
        JVar other = variables.put(var.name(), var);
        if(other!=null)
            throw new IllegalArgumentException("duplicate variable: " + var.name());
        return var;
    }

    UnitGenerator(CodeGenerator codeGenerator, BlockAnalyzer code) {
        this.codeGenerator = codeGenerator;
        this.code = code;
        JPackage jpkg = codeGenerator.subPackage(code.block.path);
        jclass = codeGenerator.defineClass(jpkg, code.block.name);
        jclass._extends(de.irt.jfor.Unit.class);
    }

    void define() {
        JDocComment comment = null;

        for (Common common : code.block.commons) {
            JDefinedClass jcommon = codeGenerator.commons.get(common.name);
            JFieldVar jvar = jclass.field(JMod.PUBLIC | JMod.FINAL,
                    jcommon, common.name,
                    JExpr.invoke("common").arg(JExpr.dotclass(jcommon)));
            if (comment == null)
                comment = jvar.javadoc();
            decl(jvar);
        }

        if (comment != null) {
            comment.setSingleLineMode(true);
            comment.add("\n");
            comment.add("commons");
            comment = null;
        }

        for (Block block : code.block.blocks) {
            String name = block.name;
            JDefinedClass junit = codeGenerator.units.get(name).jclass;
            JFieldVar jvar = jclass.field(JMod.PUBLIC | JMod.FINAL,
                    junit, name,
                    JExpr.invoke("unit").arg(JExpr.dotclass(junit)));
            if (comment == null)
                comment = jvar.javadoc();
            decl(jvar);
        }

        if (comment != null) {
            comment.setSingleLineMode(true);
            comment.add("\n");
            comment.add("units");
            comment = null;
        }

        for (Variable var : code.block.variables) {
            if (var.context == null && !var.isPrimitive()) {
                JFieldVar jvar = codeGenerator.defineVariable(jclass, var);
                if (comment == null)
                    comment = jvar.javadoc();
                decl(jvar);
            }
        }

        if (comment != null) {
            comment.setSingleLineMode(true);
            comment.add("\n");
            comment.add("variables");
            comment = null;
        }

        JMethod jmethod = jclass.method(JMod.PUBLIC, code.block.type(), "call");
        int n = 0;
        for (Variable arg : code.block.arguments) {
            ++n;
            final JVar param = jmethod.param(JMod.FINAL, arg.type(), arg.name);
            if ((n % 6) == 5) {
                param.annotate(Comment.class);
                decl(param);
            }
        }

        for (Variable var : code.block.variables) {
            if (var.context == null && var.isPrimitive()) {
                AbstractJType type = codeGenerator.codeModel._ref(var.type());
                JVar jvar = jmethod.body().decl(type, var.name);
                decl(jvar);
            }
        }

        childElements(code.be, "code").forEach(ce -> code(jmethod.body(), ce));
    }

    void code(JBlock block, Element code) {
        childElements(code).forEach(ce -> {
            switch (ce.getTagName()) {
                case "C":
                    comment(block, ce);
                    break;
                    
                default:
                    block.addSingleLineComment(ce.getTagName());
                    childElements(ce).forEach(de -> code(block, de));
                    break;
            }
        });
    }

    private void assarr(JBlock block, Element ce) {

    }

    private void assign(JBlock block, Element ce) {

    }

    private void comment(JBlock block, Element ce) {
        block.addSingleLineComment(ce.getTextContent());
    }
}
