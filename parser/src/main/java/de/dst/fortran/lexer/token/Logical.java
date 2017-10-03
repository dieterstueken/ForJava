package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Logical extends Named {

    static final Toker toker = toker(":(eq|ne|le|lt|ge|gt|and|or):", m -> new Logical(m.group(1)));

    Logical(String name) {
        super(name);
    }
}
