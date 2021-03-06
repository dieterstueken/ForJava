package de.dst.fortran.lexer.lines;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.10.17
 * Time: 14:57
 */
public class EmptyLine extends CommentLine {

    static final Pattern pattern = Pattern.compile("\\s*", Pattern.CASE_INSENSITIVE);

    static final EmptyLine EMPTY = new EmptyLine();

    public EmptyLine() {
        super("");
    }

    static EmptyLine match(String line) {
           Matcher m = pattern.matcher(line);
           if(m.matches()) {
               return EMPTY;
           }
           return null;
       }
}
