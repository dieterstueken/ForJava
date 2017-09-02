package de.dst.fortran.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.04.17
 * Time: 15:42
 */
public class LineParser extends OutputParser {

    CodeParser code;

    int linum=0;

    LineParser(Writer out) {
        super(out);

        code = new CodeParser(out);
    }

    final Pattern LINE = compile("(\\s{6}|\\t)(.*)");
    final Pattern CONT = compile("(\\s{5}\\S)(.*)");
    final Pattern LABEL = compile("(\\d[\\d\\s]{5})(.*)");
    final Pattern COMMENT = compile("\\S(.*)");
    final Pattern EMPTY = compile("\\s*");
    static final Pattern BOOLEAN = compile("\\.(eq|ne|le|lt|ge|gt|and|or)\\.");
    static final Pattern KEYWORDS = compile("(ierr|stat|file|access|recl|form)\\s*=");

    final Parser parser =
            parser(EMPTY, m->{code.endl(); return null;})
            .or(parser(LINE, m->{
                endl(); // terminate code line
                out.text("\t");
                return parseCode(m.group(2));
            })).or(parser(CONT, m->{
                nl().text("\t\t");  // just print nl
                return parseCode(m.group(2));
            })).or(parser(LABEL, m->{
                endl();
                out.text("\t");
                code.label = m.group(1).trim();
                return parseCode(m.group(2));
            })).or(parser(COMMENT, m->{
                endl();
                String c = m.group(1);
                if(!c.trim().isEmpty())
                    out.text("c", c);
                return null;
            }));

    Writer endl() {
        code.endl();
        return nl();
    }

    Writer nl() {
        out.nl();
        out.text("l", Integer.toString(linum));
        return out;
    }

    public String parse(String line) {

        ++linum;

        line = parser.parse(line);

        if(!Parser.isEmpty(line)) {
            out.text("x", line);
        }

        return null;
    }


    String parseCode(String line) {

        int c = line.lastIndexOf('!');
        String comment = c>=0 ? line.substring(c+1) : null;

        if(c>=0)
            line = line.substring(0, c);

        Matcher m = BOOLEAN.matcher(line);
        line = m.replaceAll(":$1:");

        // hide from assignment
        m = KEYWORDS.matcher(line);
        line = m.replaceAll("$1%");

        line = code.parse(line);

        if(!Parser.isEmpty(line)) {
            out.text("x", line);
        }

        if(comment!=null)
            out.text("c", comment);
        
        return null;
    }

    public void close() {
        code.close();
    }
}
