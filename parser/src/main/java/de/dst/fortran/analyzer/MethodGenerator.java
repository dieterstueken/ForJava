package de.dst.fortran.analyzer;

import com.helger.jcodemodel.*;
import de.dst.fortran.code.Variable;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.dst.fortran.analyzer.Analyzer.childElements;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  27.10.2017 11:47
 * modified by: $Author$
 * modified on: $Date$
 */
public class MethodGenerator {

    final CodeGenerator codeGenerator;
    final JDefinedClass jclass;
    JMethod jmethod = null;

    final Map<String, IJAssignmentTarget> variables = new HashMap<>();

    // debug
    String line = "";

    MethodGenerator(CodeGenerator codeGenerator, JDefinedClass jclass) {
        this.codeGenerator = codeGenerator;
        this.jclass = jclass;
    }

    JMethod method(Class type, String name) {
        if(jmethod!=null)
            throw new IllegalStateException("duplicate methode definitition");

        return jmethod = jclass.method(JMod.PUBLIC, type, name);
    }

    MethodGenerator(CodeGenerator codeGenerator, JDefinedClass jclass, Class type, String name) {
        this.codeGenerator = codeGenerator;
        this.jclass = jclass;
        method(type, name);
    }

    void decl(JVar var) {
        decl(var, var.name());
    }

    void decl(IJAssignmentTarget var, String name) {
        IJAssignmentTarget other = variables.put(name, var);
        if (other != null)
            throw new IllegalArgumentException("duplicate variable: " + var.toString());
    }

    static AbstractJType getType(IJAssignmentTarget target) {
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

    JVar param(Variable var) {
        JVar param = jmethod.param(JMod.FINAL, var.type(), var.name);
        variables.put(var.name, param);
        return param;
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
                return f -> f.print("/* ").print(code.getTagName()).print(" */");
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
        return target.assign(expr(childElements(e, "expr")));
    }

    private IJStatement assarr(Element e) {
        String name = e.getAttribute("name");
        return f -> f.print("/* ").print(name).print(" */");
    }

    IFExpression expr(List<Element> tokens) {

        IFExpression expr = IFExpression.EMPTY;

        for (Element ce : tokens)
            expr = expr.append(expr(ce));

        return expr;
    }

    IJGenerable expr(Element e) {

        switch (e.getTagName()) {

            case "expr":
                return expr(childElements(e));

            case "val":
                return val(e);

            case "var":
                return var(e);

            case "fun":
                return fun(e);

            case "b":
                return IFExpression.expr("(").append(expr(childElements(e))).append(")");

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
                return IFExpression.expr(e.getTagName() + "...");
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
            return JExpr.invoke(name);

        AbstractJType type = getType(target);
        if (codeGenerator.arrType.isAssignableFrom(type))
            return target.invoke("get");

        if(codeGenerator.unitType.isAssignableFrom(type))
            return target.invoke("call");

        throw new IllegalArgumentException("invalid function call: " + name);
    }
}
