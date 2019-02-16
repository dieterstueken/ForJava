package de.dst.fortran.code.generator.java;

import com.helger.jcodemodel.*;
import de.dst.fortran.code.*;
import de.irt.jfor.Unit;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Objects;

import static de.dst.fortran.code.Analyzer.childElement;
import static de.dst.fortran.code.Analyzer.childElements;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 21.10.17
 * Time: 15:35
 */
class UnitGenerator extends MethodGenerator {

    final CodeElement element;

    UnitGenerator(CodeGenerator codeGenerator, CodeElement element, JDefinedClass jclass) {
        super(codeGenerator, jclass);
        this.jclass._extends(de.irt.jfor.Unit.class);
        this.element = element;
    }

    void define() {
        try {
            constructor();
            decl();
            func();
            body();
        } catch(Throwable e) {
            String message = String.format("error parsing %s.%s:%s", element.code().path, element.code().name, line);
            throw new RuntimeException(message, e);
        }
    }
    
    void constructor() {
        JMethod constructor = jclass.constructor(JMod.PUBLIC);
        JVar parent = constructor.param(JMod.NONE, Unit.class, "parent");
        constructor.body().add(JExpr.invokeSuper().arg(parent));
    }

    void decl() {

        JDocComment comment = null;

        for (Common common : element.code().commons) {
            JDefinedClass jcommon = codeGenerator.commons.get(common.getName());
            JFieldVar cvar = jclass.field(JMod.PRIVATE | JMod.FINAL,
                    jcommon, common.getName(),
                    JExpr.invoke("common").arg(JExpr.dotclass(jcommon)));
            if (comment == null)
                comment = cvar.javadoc();

            // define variables for each member using local names
            common.members().forEach(member->{
                JFieldVar var = jcommon.fields().get(member.getRefName());
                JFieldRef ref = cvar.ref(var);
                decl(ref, member.getName());
            });
        }

        if (comment != null) {
            comment.setSingleLineMode(true);
            comment.add("\n");
            comment.add("commons");
            comment = null;
        }

        for (Code block : element.code().blocks) {
            String name = block.name;
            JDefinedClass junit = codeGenerator.units.get(name).jclass;
            JFieldVar jvar = jclass.field(JMod.PRIVATE | JMod.FINAL,
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

        for (Variable var : element.code().variables) {
            if (var.context == null && !var.isPrimitive()) {
                JFieldVar jvar = codeGenerator.defineVariable(jclass, var, JMod.PRIVATE);
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
        Element functions = childElement(element.getElement(), "functions");
        childElements(functions, "function").forEach(this::function);
    }

    void function(Element fun) {
        String name = fun.getAttribute("name");
        Variable var = element.code().variables.find(name);
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

        method(JMod.PUBLIC, element.code().type(), "call");

        header(childElement(element.getElement(), "decl"));

        // prepare arguments
        for (Element arg : childElements(childElement(element.getElement(), "args"), "arg")) {

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
                        Variable var = element.code().arguments.get(ce.getAttribute("name"));
                        JVar param = param(var);
                        if (nl)
                            param.annotations();
                        break;
                    }
                }
            }
        }

        //int n = 0;
        //for (Variable arg : code.block().arguments) {
        //    ++n;
        //    final JVar param = jmethod.param(JMod.FINAL, arg.type(), arg.name);
        //    if ((n % 6) == 5) {
        //        param.annotations();
        //        //param.annotate(Comment.class);
        //        //decl(param);
        //    }
        //}

        final JBlock jbody = jmethod.body();
        jbody.add(JFExpression.NL);

        // local variables
        for (Variable var : element.code().variables) {
            if (var.context == null && var.isPrimitive()) {

                final Class<?> type = codeGenerator.typeOf(var.type());
                AbstractJType jType = codeGenerator.codeModel._ref(type);
                IJExpression init = Boolean.class.isAssignableFrom(type) ? JExpr.FALSE : JExpr.lit(0);
                JVar jvar = jbody.decl(jType, var.name, init);
                decl(jvar);
            }
        }

        if (!element.code().variables.isEmpty())
            jbody.add(JFExpression.NL);

        childElements(childElement(element.getElement(), "code"))
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
