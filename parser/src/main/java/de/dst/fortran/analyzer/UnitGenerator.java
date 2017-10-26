package de.dst.fortran.analyzer;

import com.helger.jcodemodel.*;
import de.dst.fortran.code.Block;
import de.dst.fortran.code.Common;
import de.dst.fortran.code.Variable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static de.dst.fortran.analyzer.Analyzer.childElement;
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

    final Map<String, IJAssignmentTarget> variables = new HashMap<>();

    String line = "";

    void decl(JVar var) {
        decl(var, var.name());
    }

    void decl(IJAssignmentTarget var, String name) {
        IJAssignmentTarget other = variables.put(name, var);
        if (other != null)
            throw new IllegalArgumentException("duplicate variable: " + var.toString());
    }

    AbstractJType getType(IJAssignmentTarget target) {
        if (target instanceof JVar) {
            return ((JVar) target).type();
        } else if (target instanceof JFieldRef) {
            return ((JFieldRef) target).var().type();
        } else
            return null;
    }

    IJAssignmentTarget var(Element e) {
        return var(e.getAttribute("name"));
    }

    IJAssignmentTarget var(String name) {

        IJAssignmentTarget target = variables.get(name);
        if (target == null)
            throw new RuntimeException("missing variable: " + name);

        AbstractJType type = getType(target);
        if (codeGenerator.refType.isAssignableFrom(type)) {
            target = target.ref("v");
        }

        return target;
    }

    UnitGenerator(CodeGenerator codeGenerator, BlockAnalyzer code) {
        this.codeGenerator = codeGenerator;
        this.code = code;
        JPackage jpkg = codeGenerator.subPackage(code.block.path);
        jclass = codeGenerator.defineClass(jpkg, code.block.name);
        jclass._extends(de.irt.jfor.Unit.class);
    }

    void define() {
        try {
            _define();
        } catch(Throwable e) {
            String message = String.format("error parsing %s.%s:%s", code.block.path, code.block.name, line);
            throw new RuntimeException(message, e);
        }
    }

    void _define() {
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

        body();
    }

    void body() {

        JMethod jmethod = jclass.method(JMod.PUBLIC, code.block.type(), "call");
        header(jmethod, childElement(code.be, "decl"));

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
                        JVar param = jmethod.param(JMod.FINAL, var.type(), var.name);
                        variables.put(var.name, param);
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

    void header(JMethod jmethod, Element decl) {
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

    IJStatement code(Element code) {
        switch (code.getTagName()) {

            case "F":
                line = code.getAttribute("line");

            case "f":
                return null;

            case "C":
                return comment(code);

            case "assvar":
                return assvar(code);

            case "assarr":
                return assarr(code);

            default:
                return f -> f.print("// ").print(code.getTagName()).print("...");
        }
    }

    private IJStatement comment(Element e) {
        final String comment = e.getTextContent();

        if (comment.isEmpty())
            return JFormatter::newline;

        return new JSingleLineCommentStatement(comment);
    }

    private IJStatement assvar(Element e) {
        String name = e.getAttribute("name");
        IJAssignmentTarget target = var(name);
        // todo: complex values
        return target.assign(expr(childElements(e)));
    }

    private IJStatement assarr(Element e) {
        String name = e.getAttribute("name");
        return f -> f.print("// ").print(name).print(" = ...");
    }

    private IFExpression expr(List<Element> tokens) {

        IFExpression expr = IFExpression.EMPTY;

        for (Element ce : tokens)
            expr = expr.append(expr(ce));

        return expr;
    }

    private IJGenerable expr(Element ce) {

        switch (ce.getTagName()) {

            case "val":
                return val(ce);

            case "var":
                return var(ce);

            case "fun":
                return fun(ce);

            case "b":
                return IFExpression.expr("(").append(expr(childElements(ce))).append(")");

            case "add":
                return f -> f.print("+");

            case "neg":
            case "sub":
                return f -> f.print("-");

            case "mul":
                return f -> f.print("*");

            case "div":
                return f -> f.print("/");

            default:
                return IFExpression.expr(ce.getTagName() + "...");
        }
    }

    private IJGenerable val(Element e) {
        String value = e.getTextContent();

        if ("true".equals(value))
            return JExpr.lit(true);

        if ("false".equals(value))
            return JExpr.lit(false);

        int i = value.indexOf('.');
        if (i < 0) {
            i = Integer.parseInt(value);
            return JExpr.lit(i);
        }

        // float or double

        i = value.indexOf('d');

        if (i < 0)
            return JExpr.lit(Float.parseFloat(value));

        value = value.replace('d', 'E');
        return JExpr.lit(Double.parseDouble(value));
    }

    private IJGenerable fun(Element e) {

        String name = e.getAttribute("name");
        JInvocation invoke = invoke(name);
        childElements(e, "arg").forEach(arg -> invoke.arg(expr(childElements(arg))));

        return invoke;
    }
    
    private JInvocation invoke(String name) {
        IJAssignmentTarget target = variables.get(name);
        if(target==null)
            return JExpr._this().invoke(name);

        AbstractJType type = getType(target);
        if (codeGenerator.arrType.isAssignableFrom(type))
            return target.invoke("get");

        if(codeGenerator.unitType.isAssignableFrom(type))
            return target.invoke("call");

        throw new IllegalArgumentException("invalid function call: " + name);
    }
}
