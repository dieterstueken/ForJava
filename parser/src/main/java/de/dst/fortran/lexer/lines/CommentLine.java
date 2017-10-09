package de.dst.fortran.lexer.lines;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.10.17
 * Time: 11:52
 */
public class CommentLine extends Line {

    public CommentLine(String line) {
        super(line);
    }

    static final Pattern pattern = Pattern.compile("(?:\\S(?:\\t|\\s{5})|(?:\\s{1,6}!))?(.*)", Pattern.CASE_INSENSITIVE);

    static Line match(String line) {
        Matcher m = pattern.matcher(line);
        if(m.matches()) {
            return new CommentLine(m.group(1));
        }
        return null;
    }
}
