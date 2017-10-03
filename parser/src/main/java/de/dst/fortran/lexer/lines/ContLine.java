package de.dst.fortran.lexer.lines;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.10.17
 * Time: 11:53
 */
public class ContLine extends CodeLine {

    static final Pattern pattern = Pattern.compile("(\\s{5}(\\S))", Pattern.CASE_INSENSITIVE);

    public ContLine(String line, String lead, String code) {
        super(line, lead, code);
    }

    static ContLine match(String line) {
        Matcher m = pattern.matcher(line);
        if(m.lookingAt()) {
            String lead = m.group();
            String code = line.substring(m.end());
            return new ContLine(line, lead, code);
        }
        return null;
    }
}
