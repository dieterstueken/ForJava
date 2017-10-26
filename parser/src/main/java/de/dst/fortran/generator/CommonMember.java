package de.dst.fortran.generator;

import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldRef;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JMod;
import de.dst.fortran.code.Variable;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  24.10.2017 13:39
 * modified by: $Author$
 * modified on: $Date$
 */
public class CommonMember {

    final FortranClass unit;
    final CommonClass commonClass;

    final JFieldVar field;

    public CommonMember(FortranClass unit, CommonClass commonClass) {
        this.unit = unit;
        this.commonClass = commonClass;

        // define as member variable
        this.field = unit.jclass.field(JMod.PUBLIC | JMod.FINAL,
                commonClass.jclass, commonClass.jclass.name().toLowerCase(),
                JExpr.invoke("common").arg(JExpr.dotclass(commonClass.jclass)));
    }

    // define local members with possibly different names
    void parse(Element elem) {

        final List<Variable> members = new ArrayList<>();

        for (Element arg : elem.elements()) {
            Variable var = unit.arg(arg);
            var.context(commonClass);
            members.add(var);
        }

        for (int i = 0; i < members.size(); i++) {
            Variable member = members.get(i);
            JFieldVar var = commonClass.members.get(i);
            JFieldRef ref = field.ref(var);
        }

    }
}
