package de.dst.fortran.generator;

import com.helger.jcodemodel.JDefinedClass;
import de.dst.fortran.code.Variable;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  24.10.2017 13:06
 * modified by: $Author$
 * modified on: $Date$
 */
public class FortranCode extends FortranClass {

    final List<Variable> arguments = new ArrayList<>();

    public FortranCode(CodeGenerator generator, JDefinedClass jclass) {
        super(generator, jclass);
    }

    void parse(Element code) {

        code.element("args").elements("arg").stream().map(this::arg).forEach(arguments::add);
        code.element("decl").elements().forEach(this::decl);
        code.element("code").elements().forEach(this::code);
    }
    
    private void decl(Element decl) {
        switch (decl.getName()) {

            case "common":
                common(decl);
                break;

            case "dim":
                dim(decl);
                break;
        }
    }

    private void dim(Element dim) {

    }

    private void common(Element ce) {
        String name = ce.attributeValue("name");

    }

    private void code(Element code) {

    }


}
