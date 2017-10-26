package de.dst.fortran.generator;

import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JFieldVar;
import de.dst.fortran.code.Variable;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  23.10.2017 13:50
 * modified by: $Author$
 * modified on: $Date$
 */
public class CommonClass extends FortranClass {

    final List<JFieldVar> members = new ArrayList<>();

    public CommonClass(CodeGenerator generator, JDefinedClass jclass, Element common) {
        super(generator, jclass);
        jclass._extends(de.irt.jfor.Common.class);

        common.elements().stream()
                .map(this::var)
                .map(this::defineMember)
                .forEach(members::add);
    }

    public JFieldVar defineMember(Variable var) {
        JFieldVar member =  super.defineMember(var);
        members.add(member);
        return member;
    }
}
