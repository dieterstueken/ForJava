package de.dst.fortran.analyzer;

import com.helger.jcodemodel.*;
import de.dst.fortran.code.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Objects;

import static de.dst.fortran.analyzer.Analyzer.childElement;
import static de.dst.fortran.analyzer.Analyzer.childElements;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 21.10.17
 * Time: 15:35
 */
class UnitGenerator extends MethodGenerator {

    final BlockAnalyzer code;

    UnitGenerator(CodeGenerator codeGenerator, BlockAnalyzer code, JDefinedClass jclass) {
        super(codeGenerator, jclass);
        this.jclass._extends(de.irt.jfor.Unit.class);
        this.code = code;
    }

    void define() {
        try {
            decl();
            func();
            body();
        } catch(Throwable e) {
            String message = String.format("error parsing %s.%s:%s", code.block.path, code.block.name, line);
            throw new RuntimeException(message, e);
        }
    }

    void decl() {
        JDocComment comment = null;

        for (Common common : code.block.commons) {
            JDefinedClass jcommon = codeGenerator.commons.get(common.name);
            JFieldVar cvar = jclass.field(JMod.PUBLIC | JMod.FINAL,
                    jcommon, common.name,
                    JExpr.invoke("common").arg(JExpr.dotclass(jcommon)));
            if (comment == null)
                comment = cvar.javadoc();

            // define variables for each member using local names
            for (Variable member : common.members) {
                JFieldVar var = jcommon.fields().get(member.getRefName());
                JFieldRef ref = cvar.ref(var);
                decl(ref, member.getName());
            }
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
    }

    void func() {
        Element functions = childElement(code.be, "functions");
        childElements(functions, "function").forEach(this::function);
    }

    void function(Element fun) {
        String name = fun.getAttribute("name");
        Variable var = code.block.variables.find(name);
        if(var==null || var.context!= Context.FUNCTION)
            throw new IllegalArgumentException("undefined function: " + name);

        Element assarr = childElement(fun, "assarr");

        MethodGenerator method = new MethodGenerator(codeGenerator, jclass);
        method.method(JMod.PUBLIC|JMod.STATIC, var.type(), name);
        method.jmethod.mods();

        Entities<Variable> arguments = new Entities<>(Variable::new);

        childElements(childElement(assarr, "args"), "arg").stream()
                .flatMap(arg -> childElements(arg, "var").stream())
                .map(v->v.getAttribute("name"))
                .map(arguments::get)
                .forEach(method::param);

        JDocComment comment = method.jmethod.javadoc();
        for (Element ce : childElements(fun)) {
            switch (ce.getTagName()) {
                case "F":
                    line = ce.getAttribute("line");
                    break;

                case "C":
                    String text = ce.getTextContent();
                    comment.add(text);
                    comment.add("\n");
                    break;
            }
        }

        method.line = line;
        final JBlock jbody = method.jmethod.body();

        // single line expression
        jbody._return(method.expr(childElements(assarr, "expr")));
    }

    void body() {

        method(JMod.PUBLIC, code.block.type(), "call");

        header(childElement(code.be, "decl"));

        // prepare arguments
        for (Element arg : childElements(childElement(code.be, "args"), "arg")) {

            boolean nl = false;
            for (Element ce : childElements(arg)) {
                switch (ce.getTagName()) {
                    case "F":
                        line = ce.getAttribute("line");
                        break;
                    case "f":
                        nl = true;
                        break;
                    case "var": {
                        Variable var = code.block.arguments.get(ce.getAttribute("name"));
                        JVar param = param(var);
                        if (nl)
                            param.annotations();
                        break;
                    }
                }
            }
        }

        //int n = 0;
        //for (Variable arg : code.block.arguments) {
        //    ++n;
        //    final JVar param = jmethod.param(JMod.FINAL, arg.type(), arg.name);
        //    if ((n % 6) == 5) {
        //        param.annotations();
        //        //param.annotate(Comment.class);
        //        //decl(param);
        //    }
        //}

        final JBlock jbody = jmethod.body();
        jbody.add(JFormatter::newline);

        // local variables
        for (Variable var : code.block.variables) {
            if (var.context == null && var.isPrimitive()) {
                AbstractJType type = codeGenerator.codeModel._ref(var.type());
                JVar jvar = jbody.decl(type, var.name);
                decl(jvar);
            }
        }

        if (!code.block.variables.isEmpty())
            jbody.add(JFormatter::newline);

        childElements(childElement(code.be, "code"))
                .stream()
                .map(this::code)
                .filter(Objects::nonNull)
                .forEach(jbody::add);
    }

    void header(Element decl) {
        if (decl == null)
            return;

        JDocComment comment = jmethod.javadoc();

        for (Element ce : childElements(decl)) {
            switch (ce.getTagName()) {
                case "F":
                    line = ce.getAttribute("line");

                case "C":
                case "f":
                    comment.add(ce.getTextContent());
                    comment.add("\n");
                    break;
            }
        }

        for (Node node = decl.getFirstChild(); node != null; node = node.getNextSibling()) {

            if (node.getNodeType() == Node.COMMENT_NODE || "C".equals(node.getNodeName())) {
                comment.add(node.getTextContent());
                comment.add("\n");
            }
        }
    }
}
