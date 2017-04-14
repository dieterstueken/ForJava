package de.dst.fortran.code;

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

    public Type(String name, String id) {
        this.id = id;
        this.name = name;
    }

    public String toString() {
        return id;
    }

    public static Type stringOf(int dim) {
        String name = String.format("character*%d", dim);
        return new Type(name.substring(9), name);
    }

    public static final Type CH = new Type("character*1", "CH");

    public static final Type I2 = new Type("integer*2", "I2");
    public static final Type I4 = new Type("integer*4", "I4");

    public static final Type R4 = new Type("real*4", "R4");
    public static final Type R8 = new Type("real*8", "R8");

    public static final Type CX = new Type("complex", "CX");

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
