package de.dst.fortran.analyzer;

import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.IJGenerable;
import com.helger.jcodemodel.JFormatter;

import javax.annotation.Nonnull;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  26.10.2017 11:03
 * modified by: $Author$
 * modified on: $Date$
 */
public interface IFExpression extends IJExpression {

    IFExpression EMPTY = new IFExpression() {
        @Override
        public void generate(@Nonnull JFormatter f) {}

        @Override
        public IFExpression append(IJGenerable expr) {
            if(expr instanceof IFExpression)
                return (IFExpression) expr;
            else
                return IFExpression.super.append(expr);
        }
    };

    IFExpression NL = JFormatter::newline;

    static IFExpression expr(IJGenerable expr) {
        return EMPTY.append(expr);
    }

    static IFExpression expr(char fragment) {
        return f->f.print(fragment);
    }

    static IFExpression expr(String fragment) {
        return fragment==null || fragment.isEmpty() ? EMPTY : f->f.print(fragment);
    }

    default IFExpression append(IJGenerable expr) {
        return expr==EMPTY||expr==null ? this : f-> f.generable(this).generable(expr);
    }

    default IFExpression append(String code) {
        return append(expr(code));
    }

    default IFExpression nl() {
        return append(NL);
    }
}
