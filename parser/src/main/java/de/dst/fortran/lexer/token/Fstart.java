package de.dst.fortran.lexer.token;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 03.10.17
 * Time: 22:27
 */
public class Fstart extends Named {

    public Fstart(File file) {
        super(name(file));
    }

    static String name(File file) {
        String name = file.getName();
        int dot = name.indexOf('.');
        if(dot>=0)
            return name.substring(0, dot);
        else
            return name;
    }
}
