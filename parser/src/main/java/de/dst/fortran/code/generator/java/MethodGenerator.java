package de.dst.fortran.code.generator.java;

import com.helger.jcodemodel.*;
import de.dst.fortran.code.TypeDef;
import de.dst.fortran.code.Variable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.*;
import java.util.stream.Stream;

import static de.dst.fortran.code.Analyzer.childElement;
import static de.dst.fortran.code.Analyzer.childElements;

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

    JMethod method(int mod, TypeDef type, String name) {
        if(jmethod!=null)
            throw new IllegalStateException("duplicate methode definitition");

        return jmethod = jclass.method(mod, codeGenerator.typeOf(type), name);
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
        IJAssignmentTarget var = var(e.getAttribute("name"));

        // possibly pass by reference
        if("true".equals(e.getAttribute("returned"))) {
            return var;
        } else
            return deref(var);
    }

    IJAssignmentTarget var(String name) {

        IJAssignmentTarget target = variables.get(name);
        if (target == null)
            return null;

        return target;
    }

    boolean isComplex(IJAssignmentTarget target) {
        AbstractJType type = getType(target);
        return codeGenerator.cplxType.isAssignableFrom(type);
    }

    IJAssignmentTarget deref(IJAssignmentTarget target) {

        AbstractJType type = getType(target);

        if (codeGenerator.refType.isAssignableFrom(type)
                && !codeGenerator.arrType.isAssignableFrom(type)) {
            target = target.ref("v");
        }

        return target;
    }

    JVar param(Variable var) {
        JVar param = jmethod.param(JMod.FINAL, codeGenerator.typeOf(var.typeDef()), var.name);
        variables.put(var.name, param);
        return param;
    }

    IJStatement code(Element code) {
        if(code==null)
            return null;

        final String name = code.getTagName();
        switch (name) {

            case "F":
                line = code.getAttribute("line");
                return null;

            case "f":
                return JFExpression.NL;

            case "C":
                return comment(code);

            case "assvar":
                return assvar(code);

            case "assarr":
                return assarr(code);

            case "call":
                return call(code);

            case "goto":
                return _goto(code);

            case "ifblock":
                return _if(code);

            case "do":
                return _do(code);

            case "cycle":
                return new JBreak(null);

            case "exit":
                return new JContinue(null);

            case "return":
                return _return();

            default:
                return f -> f.print("/* ").print(name).print(" */");
        }
    }

    JReturn _return() {
        AbstractJType type = jmethod.type();
        if(type== type.owner().VOID)
            return JFExpression._return();

        final IJAssignmentTarget var = var(jclass.name().toLowerCase());
        return JFExpression._return(deref(var));
    }

    Stream<IJStatement> statements(Element e) {
        return statements(childElements(e));
    }

    Stream<IJStatement> statements(Collection<Element> elements) {
        return elements.stream().map(this::code).filter(Objects::nonNull);
    }

    private IJStatement _if(Element code) {
        List<Element> elements = childElements(code);

        while(!elements.isEmpty()) {

            Element cond = elements.remove(0);
            String type = cond.getTagName();

            if("locals".equals(type))
                continue;   // skip

            if (!"if".equals(cond.getTagName()))
                throw new IllegalArgumentException("condition expected, got: " + cond.getTagName());

            return _if(expr(childElements(cond)), elements);
        }

        throw new IllegalStateException("broken if block");
    }

    // populate given conditional
    private JConditional _if(IJExpression cond, List<Element> elements) {
        JConditional _if = JFExpression._if(cond);

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

    private IJStatement _goto(Element _goto) {
        return f->f.print("/* goto ").print(_goto.getTextContent()).print("*/");
    }

    private IJStatement _do(Element code) {
        List<Element> body = childElements(code);

        while(!body.isEmpty()) {
            Element cond = body.remove(0);
            final String type = cond.getTagName();

            switch (type) {
                case "while":
                    return _while(cond, body);

                case "for":
                    return _for(cond, body);

                case "locals":
                    break; // skip

                default:
                    throw new IllegalArgumentException("unknown do loop statement: " + type);
            }
        }

        throw new IllegalStateException("broken do loop");
    }

    private IJStatement _while(Element cond, List<Element> body) {
        JWhileLoop _while = new JWhileLoop(expr(cond)) {};
        statements(body).forEach(_while.body()::add);
        return _while;
    }

    private IJStatement _for(Element cond, List<Element> body) {

        IJAssignmentTarget target = deref(var(cond));

        if(!(target instanceof JVar))
            throw new ClassCastException();

        JVar var = (JVar) target;

        List<Element> args = childElements(cond, "arg");

        JForLoop _for = new JForLoop() {};

        _for.init((JVar) target, expr(args.get(0)));
        _for.test(var.lte(expr(args.get(1))));
        _for.update(args.size()>2 ? var.assignPlus(expr(args.get(2))) : var.preincr());

        statements(body).forEach(_for.body()::add);
        return _for;
    }


    private IJStatement comment(Element e) {
        String comment = e.getTextContent();

        if (comment.isEmpty())
            return JFExpression.NL;

        if(comment.charAt(0)=='+') {
            comment = "+" + comment.substring(1).trim();
        } else
            comment = comment.trim();

        return new JSingleLineCommentStatement(comment);
    }

    private IJStatement assvar(Element e) {
        String name = e.getAttribute("name");
        IJAssignmentTarget target = var(name);

        final JFExpression expr = expr(childElements(e));

        if(isComplex(target)) {
            // resolve assign(Complex.of...
            if(expr instanceof JFComplex) {
                JFComplex cplx = (JFComplex) expr;
                return target.invoke("assign")
                        .arg(cplx.re())
                        .arg(cplx.im());
            } else
                return target.invoke("assign").arg(expr);
        } else {
            return deref(target).assign(expr);
        }
    }

    private IJStatement assarr(Element e) {
        String name = e.getAttribute("name");
        IJAssignmentTarget var = var(name);

        JInvocation invoke = var.invoke("set");
        childElements(childElement(e, "args"), "arg")
                .stream()
                .map(this::expr)
                .forEach(invoke::arg);

        invoke.arg(expr(childElement(e, "expr")));

        return invoke;
    }

    JFExpression expr(List<Element> tokens) {

        pow(tokens);

        JFExpression expr = JFExpression.EMPTY;

        for (Element ce : tokens)
            expr = expr.append(expr(ce));

        return expr;
    }

    // rearrange pow operator(s)
    List<Element> pow(List<Element> elements) {

        for(int i=0; i<elements.size(); ++i) {
            Element pow = elements.get(i);
            String name = pow.getNodeName();
            if(!"pow".equals(name))
                continue;

            Node prev = pow.getPreviousSibling();
            Node next = pow.getNextSibling();
            if(prev==null || next==null)
                throw new IllegalStateException("missing pow rhs");

            pow.appendChild(prev);
            pow.appendChild(pow.getOwnerDocument().createTextNode(","));
            pow.appendChild(next);

            elements.remove(i+1);
            elements.remove(i-1);
            i-=1;
        }

        return elements;
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

            case "F":
                line = e.getAttribute("line");
                return null;

            case "expr":
            case "while":
                return expr(childElements(e));

            case "arg":
                return arg(e);

            case "val":
                return val(e);

            case "var":
                return var(e);

            case "fun":
                return fun(e);

            case "pow":
                return pow(e);

            case "cat":
                return cat(e);

            case "b":
                return JFExpression.expr("(").append(expr(childElements(e))).append(")");

            case "string":
                 return _string(e);

            case "c":
                return f->f.print("/* ").print(e.getTextContent()).print(" */");

            default:
                return JFExpression.expr(e.getTagName() + "...");
        }
    }

    IJExpression _string(Element e) {
        String text = e.getTextContent();
        if(text.length()==1)
            return JExpr.lit(text.charAt(0));
        else
            return JExpr.lit(text);
    }

    private IJExpression arg(Element e) {
        IJExpression expr = expr(childElements(e));

        if("true".equals(e.getAttribute("returned"))) {
            expr = JExpr.invoke("ref").arg(expr);
        }

        return expr;
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

    private IJExpression cat(Element e) {

        Element arg = childElement(e, "string");
        String text = arg.getTextContent();
        return f-> f.print(".cat(\"").print(text).print("\")");
    }

    private IJExpression pow(Element e) {
        List<Element> pow = childElements(e);
        if(pow.size()!=2)
            throw new IllegalArgumentException("pow");
        return JExpr.invoke("pow")
                .arg(expr(pow.get(0)))
                .arg(expr(pow.get(1)));
    }

    private IJStatement call(Element call) {
        String name = call.getAttribute("name");
        return invoke(name, childElements(call, "arg"));
    }

    private IJExpression fun(Element e) {

        String name = e.getAttribute("name");

        if("float".equals(name)) {
            name = "_float";
            //return JExpr.cast(generators.codeModel.FLOAT, expr(childElement(e, "arg")));
        }

        if("int".equals(name)) {
            //return JExpr.cast(generators.codeModel.SHORT, expr(childElement(e, "arg")));
            name = "_int";
        }

        if("cmplx".equals(name)) {
            List<Element> args = childElements(e, "arg");
            if(args.size()==2) {
                IJExpression re = expr(args.get(0));
                IJExpression im = expr(args.get(1));
                return codeGenerator.complex(re, im);
            }
        }

        return invoke(name, childElements(e, "arg"));
    }

    private JInvocation invoke(String name, List<Element> args) {
        JInvocation invoke = invoke(name);
        args.forEach(arg -> invoke.arg(expr(arg)));
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
