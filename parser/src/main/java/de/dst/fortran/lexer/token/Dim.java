package de.dst.fortran.lexer.token;

import de.dst.fortran.lexer.LineBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Dim extends Token {

    static final Toker<Dim> toker = toker("((integer|real|character)(\\*\\d+)?)", m -> new Dim(m.group(1), m.group(2)));

    static Dim dim(String dim) {
        if(dim == null)
            return null;

        return toker.match(new LineBuffer(dim));
    }

    final String type;

    final String size;

    Dim(String type, String size) {
        this.type = type;
        this.size = size;
    }
}
