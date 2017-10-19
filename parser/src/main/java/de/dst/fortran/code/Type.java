package de.dst.fortran.code;


import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JExpr;
import de.irt.jfor.*;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  07.02.13 18:55
 * modified by: $Author$
 * modified on: $Date$
 */
public class Type {

    public final String id;
    public final String name;

    public final Class<?> type;
    public IJExpression init;

    public Type(String name, String id, Class<?> type, IJExpression init) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.init = init;
    }

    public Type(String name, String id, Class<?> type, String init) {
        this(name, id, type, JExpr.invoke(init));
    }

    public Type(String name, String id, Class<?> type) {
        this(name, id, type, JExpr.invoke(id));
    }

    public Class<?> getJType() {
        return type;
    }

    public IJExpression init() {
        return init;
    }

    public String toString() {
        return id;
    }

    public static Type stringOf(int dim) {
        String name = String.format("character*%d", dim);
        return new Type(name, "CH", Chars.class, JExpr.invoke("chars"));
    }

    public static final Type CH = new Type("character*1", "CH", Ch.class, "chars");

    public static final Type I = new Type("integer", "I", I2.class, "i2");
    public static final Type I2 = new Type("integer*2", "I2", I2.class, "i2");
    public static final Type I4 = new Type("integer*4", "I4", I4.class, "i4");

    public static final Type L4 = new Type("logical*4", "L4", L4.class, "l4");

    public static final Type R = new Type("real", "R", R4.class, "r4");
    public static final Type R4 = new Type("real*4", "R4", R4.class, "r4");
    public static final Type R8 = new Type("real*8", "R8", R8.class, "r8");

    public static final Type CX = new Type("complex", "CX", Complex.class, "complex");

    public static Type intrinsic(String name) {
        return "ijklmn".indexOf(Character.toLowerCase(name.charAt(0)))>=0 ? I : R;
    }

    public static Type parse(final String token) {
        if(token==null)
            return null;

        if(token.startsWith(CH.name))
            return CH;

        if(token.startsWith(I2.name))
            return I2;

        if(token.startsWith(I4.name))
            return I4;

        if(token.startsWith(R4.name))
            return R4;

        if(token.startsWith(R8.name))
            return R8;

        if(token.startsWith("character*")) {
            Integer dim = parseInt(token.substring(10));
            if(dim==null)
                return stringOf(1);
            else
                return stringOf(dim);
        }

        return null;
    }

    public static Integer parseInt(String line) {

        if(line==null || line.isEmpty())
            return null;

        char ch = line.charAt(0);
        if(!Character.isDigit(ch))
            return null;

        int label = 0;

        int len = line.length();
        for(int i=0; i<len; ++i) {
            ch = line.charAt(i);
            if(!Character.isDigit(ch))
                break;
            label = 10 * label + Character.getNumericValue(ch);
        }

        return label;
    }
}
