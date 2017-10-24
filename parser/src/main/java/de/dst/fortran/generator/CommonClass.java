package de.dst.fortran.generator;

import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JFieldVar;
import de.dst.fortran.code.Variable;
import org.dom4j.Element;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  23.10.2017 13:50
 * modified by: $Author$
 * modified on: $Date$
 */
public class CommonClass extends FortranClass {

    final Map<String, JFieldVar> members = new LinkedHashMap<>();

    public CommonClass(CodeGenerator generator, JDefinedClass jclass, Element common) {
        super(generator, jclass);
        jclass._extends(de.irt.jfor.Common.class);
    }

    public JFieldVar defineMember(Variable var) {
        JFieldVar member =  super.defineMember(var);
        members.put(member.name(), member);
        return member;
    }
}
