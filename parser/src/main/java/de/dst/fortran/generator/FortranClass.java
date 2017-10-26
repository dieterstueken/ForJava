package de.dst.fortran.generator;

import com.helger.jcodemodel.*;
import de.dst.fortran.code.Constant;
import de.dst.fortran.code.Context;
import de.dst.fortran.code.Value;
import de.dst.fortran.code.Variable;
import de.irt.jfor.Ref;
import org.dom4j.Element;

import java.util.LinkedHashMap;
import java.util.Map;

import static de.dst.fortran.code.Value.UNDEF;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  23.10.2017 13:52
 * modified by: $Author$
 * modified on: $Date$
 */
public class FortranClass implements Context {

    final CodeGenerator generator;

    final JDefinedClass jclass;

    final Map<String, Variable> variables = new LinkedHashMap<>();

    public FortranClass(CodeGenerator generator, JDefinedClass jclass) {
        this.jclass = jclass;
        this.generator = generator;
    }

    @Override
    public String getName() {
        return jclass.name();
    }

    protected RuntimeException unexpected(Element elem) {
        throw new IllegalArgumentException("unexpected element:" + elem.toString());
    }

    protected Variable var(Element elem) {
        if(elem==null)
            return null;

        String name = elem.attributeValue("name");
        if(name==null)
            throw unexpected(elem);

        Variable var = variables.computeIfAbsent(name, Variable::new);

        return var;
    }

    protected Variable arg(Element var) {

        switch(var.getName()) {
            case "var":
                return var(var);
            case "arr":
                return arr(var);
            default:
        }

        throw unexpected(var);
    }

    protected Variable arr(Element elem) {
        Variable var = var(elem);

        for (Element de : elem.elements()) {
            switch(de.getName()) {

                case "var":
                    var.dim(var(de));
                    break;

                case "val":
                    var.dim(Constant.of(elem.getText()));
                    break;

                default:
                    throw unexpected(de);
            }
        }

        return var;
    }

    public JFieldVar defineMember(Variable var) {
        Class<?> type = var.type();

        IJExpression expr = null;

        if(Ref.class.isAssignableFrom(type)) {
            JInvocation init = jclass.owner().ref(type).staticInvoke("of");

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

        return jclass.field(JMod.PUBLIC|JMod.FINAL, type, var.name, expr);
    }
}
