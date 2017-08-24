package de.dst.fortran.parser;

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
    final Pattern EMPTY = compile("\\s{0,5}");

    Parser parser =
            parser(EMPTY, m->{code.nl(); return null;})
            .or(parser(LINE, m->{
                code.nl(); // terminate code line
                out.text("\t");
                return parseCode(m.group(2));
            })).or(parser(CONT, m->{
                out.nl().text("\t\t");  // just print nl
                return parseCode(m.group(2));
            })).or(parser(LABEL, m->{
                code.nl();
                out.text("\t");
                code.label = m.group(1).trim();
                return parseCode(m.group(2));
            })).or(parser(COMMENT, m->{
                code.nl();
                String c = m.group(1);
                if(!c.trim().isEmpty())
                    out.text("c", c);
                return null;
            }));


    public String parse(String line) {

        ++linum;

        line = parser.parse(line);

        if(line!=null && line.isEmpty()) {
            out.text("x", line);
        }

        return null;
    }


    String parseCode(String line) {

        int c = line.lastIndexOf('!');
        String comment = c>=0 ? line.substring(c+1) : null;

        line = code.parse(c<0 ? line : line.substring(0, c));


        if(line!=null && line.isEmpty()) {
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
