package de.dst.fortran.analyzer;

import com.helger.jcodemodel.*;
import de.dst.fortran.code.Variable;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static de.dst.fortran.analyzer.Analyzer.childElement;
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

    JMethod method(int mod, Class type, String name) {
        if(jmethod!=null)
            throw new IllegalStateException("duplicate methode definitition");

        return jmethod = jclass.method(mod, type, name);
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
            return null;

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
        if(code==null)
            return null;

        switch (code.getTagName()) {

            case "F":
                line = code.getAttribute("line");

            case "f":
                return JFormatter::newline;

            case "C":
                return comment(code);

            case "assvar":
                return assvar(code);

            case "assarr":
                return assarr(code);

            case "if":
                return _if(code);

            case "return":
                return _return();

            default:
                return f -> f.print("/* ").print(code.getTagName()).print(" */");
        }
    }

    JReturn _return() {
        AbstractJType type = jmethod.type();
        if(type== type.owner().VOID)
            return IFExpression._return();

        final IJAssignmentTarget var = var(jclass.name().toLowerCase());
        return IFExpression._return(var);
    }

    Stream<IJStatement> statements(Element e) {
        return childElements(e).stream().map(this::code).filter(Objects::nonNull);
    }

    private IJStatement _if(Element code) {
        List<Element> elements = childElements(code);
        Element cond = elements.remove(0);
        if(!"cond".equals(cond.getTagName()))
            throw new IllegalArgumentException("condition expected, got: " + cond.getTagName());

        return _if(expr(childElements(cond)), elements);
    }

    // populate given conditional
    private JConditional _if(IJExpression cond, List<Element> elements) {
        JConditional _if = IFExpression._if(cond);

        done:
        while(!elements.isEmpty()) {
            Element e = elements.remove(0);
            String name = e.getTagName();
            switch(name) {
                case "then":
                    // add code lines
                    statements(e).forEach(_if._then()::add);
                    break;

                case "else":
                    // add code lines
                    statements(e).forEach(_if._else()::add);
                    break done;

                case "elif":
                    _if._else().add(_if(expr(childElements(e)), elements))
                            .bracesRequired(false).indentRequired(false);
                    break done;
            }
        }

        if(!elements.isEmpty())
            throw new IllegalArgumentException("non terminal else: " + elements.get(0).getTagName());

        return _if;
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
        return f -> f.print("/* ").print(name).print(" */");
    }

    IFExpression expr(List<Element> tokens) {

        IFExpression expr = IFExpression.EMPTY;

        for (Element ce : tokens)
            expr = expr.append(expr(ce));

        return expr;
    }

    static final Map<String, IJExpression> OPERATORS = new HashMap();

    static {
        OPERATORS.put("add", f -> f.print("+"));
        OPERATORS.put("sub", f -> f.print("-"));
        OPERATORS.put("neg", f -> f.print("-"));
        OPERATORS.put("mul", f -> f.print("*"));
        OPERATORS.put("div", f -> f.print("/"));

        OPERATORS.put("eq", f -> f.print("=="));
        OPERATORS.put("ne", f -> f.print("!="));
        OPERATORS.put("le", f -> f.print("<="));
        OPERATORS.put("lt", f -> f.print("<"));
        OPERATORS.put("ge", f -> f.print(">="));
        OPERATORS.put("gt", f -> f.print(">"));

        OPERATORS.put("and", f -> f.print("&&"));
        OPERATORS.put("or", f -> f.print("||"));
        OPERATORS.put("f", f->f.newline().print("    "));
    }

    IJExpression expr(Element e) {

        String tag = e.getTagName();
        IJExpression op = OPERATORS.get(tag);
        if(op!=null)
            return op;

        switch (tag) {

            case "expr":
            case "arg":
                return expr(childElements(e));

            case "val":
                return val(e);

            case "var":
                return var(e);

            case "fun":
                return fun(e);

            case "b":
                return IFExpression.expr("(").append(expr(childElements(e))).append(")");

            default:
                return IFExpression.expr(e.getTagName() + "...");
        }
    }

    private IJExpression val(Element e) {
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

    private IJExpression fun(Element e) {

        String name = e.getAttribute("name");

        if("float".equals(name)) {
            return JExpr.cast(codeGenerator.codeModel.FLOAT, expr(childElement(e, "arg")));
        }

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
