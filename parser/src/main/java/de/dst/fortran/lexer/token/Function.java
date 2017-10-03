package de.dst.fortran.lexer.token;

import java.util.regex.Matcher;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Function extends Named {

    static final Toker toker = toker("(\\w+\\S*)?\\s*function\\s*(\\w+)\\s*\\(", Function::function);

    final Dim dim;

    Function(Dim dim, String name) {
        super(name);
        this.dim = dim;
    }

    static Function function(Matcher m) {
        String dim = m.group(1);
        String name = m.group(2);
        return new Function(Dim.dim(dim), name);
    }
}
