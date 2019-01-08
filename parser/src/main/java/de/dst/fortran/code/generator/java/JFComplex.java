package de.dst.fortran.code.generator.java;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JInvocation;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 28.10.17
 * Time: 15:12
 */
public class JFComplex extends JInvocation implements JFExpression {

    IJExpression re;
    IJExpression im;

    public JFComplex(AbstractJClass cref, IJExpression re, IJExpression im) {
        super(cref,"of");
        this.re = re;
        this.im = im;
        arg(re);
        arg(im);
    }

    public IJExpression re() {
        return re;
    }

    public IJExpression im() {
        return im;
    }
}
