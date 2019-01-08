package de.dst.fortran.code.generator.java;

import com.helger.jcodemodel.*;

import javax.annotation.Nonnull;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  26.10.2017 11:03
 * modified by: $Author$
 * modified on: $Date$
 */
public interface JFExpression extends IJExpression {

    JFExpression EMPTY = new JFExpression() {
        @Override
        public void generate(@Nonnull IJFormatter f) {}

        @Override
        public JFExpression append(IJGenerable expr) {
            if(expr instanceof JFExpression)
                return (JFExpression) expr;
            else
                return JFExpression.super.append(expr);
        }
    };

    IJStatement NL = IJFormatter::newline;

    static JFExpression expr(IJGenerable expr) {
        return EMPTY.append(expr);
    }

    static JFExpression expr(char fragment) {
        return f->f.print(fragment);
    }

    static JFExpression expr(String fragment) {
        return fragment==null || fragment.isEmpty() ? EMPTY : f->f.print(fragment);
    }

    default JFExpression append(IJGenerable expr) {
        return expr==EMPTY||expr==null ? this : f-> f.generable(this).generable(expr);
    }

    default JFExpression append(String code) {
        return append(expr(code));
    }

    default IJStatement nl() { return NL; }

    static JConditional _if(IJExpression expr) {
        return new JConditional(expr) {};
    }

    static JReturn _return () {
        return new JReturn(null) {};
    }

    static JReturn _return (IJExpression expr) {
        return new JReturn(expr) {};
    }
}
