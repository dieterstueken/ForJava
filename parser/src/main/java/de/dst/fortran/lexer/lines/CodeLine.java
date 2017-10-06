package de.dst.fortran.lexer.lines;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.10.17
 * Time: 11:53
 */
public class CodeLine extends Line {

    static final Pattern BOOLEAN = Pattern.compile("\\.(eq|ne|le|lt|ge|gt|and|or)\\.", Pattern.CASE_INSENSITIVE);

    public final String lead;

    public final String code;

    public CodeLine(String line, String lead, String code) {
        super(line);
        this.lead = lead;

        Matcher m = BOOLEAN.matcher(code);
        code = m.replaceAll(":$1:");

        this.code = code;
    }
}
