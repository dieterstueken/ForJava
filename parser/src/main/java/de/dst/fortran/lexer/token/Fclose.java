package de.dst.fortran.lexer.token;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 14:41
 */
class Fclose extends Named {

    static final Toker toker = toker("close\\s*\\(\\s*(.*)\\s*\\)\\s", m -> new Fclose(m.group(1)));

    Fclose(String name) {
        super(name);
    }
}
