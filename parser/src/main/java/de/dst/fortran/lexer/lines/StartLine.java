package de.dst.fortran.lexer.lines;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.10.17
 * Time: 15:18
 */
public class StartLine extends CodeLine {

    public StartLine(String line, String lead, String code) {
        super(line, lead, code);
    }

    public String label() {
        if(!lead.isEmpty() && Character.isDigit(lead.charAt(0))) {
            int i = lead.length();
            while(i>0 && lead.charAt(i-1)==' ')
                --i;

            return lead.substring(0, i);
        } else
            return null;
    }
    
    static final Pattern pattern = Pattern.compile("\\s{6}|\\t|\\d[\\d\\s]{5}", Pattern.CASE_INSENSITIVE);

    static CodeLine match(String line) {
        Matcher m = pattern.matcher(line);
        if(m.lookingAt()) {
            String label = m.group();
            String code = line.substring(m.end());
            return new StartLine(line, label, code);
        }

        return null;
    }
}
