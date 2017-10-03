package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Comment extends Named {

    static final Toker toker = toker("!(.*)", m -> new Comment(m.group(1)));

    Comment(String name) {
        super(name);
    }
}
