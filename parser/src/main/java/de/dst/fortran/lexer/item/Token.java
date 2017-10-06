package de.dst.fortran.lexer.item;

import java.util.AbstractList;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 06.10.17
 * Time: 11:30
 */
public class Token extends AbstractList<String> {

    public final Item item;

    public Token(Item item) {
        this.item = item;
    }

    @Override
    public String get(int index) {
        throw new IndexOutOfBoundsException("empty item");
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public String toString() {
        return item.toString();
    }
}
