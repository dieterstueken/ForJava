package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.10.17
 * Time: 19:06
 */
public class BlockData extends Named {

    static final Toker toker = toker("block\\s+data\\s+(\\w+)\\s*", m -> new BlockData(m.group(1)));

    BlockData(String name) {
        super(name);
    }
}