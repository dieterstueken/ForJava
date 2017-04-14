package de.dst.fortran.parser;

import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.04.17
 * Time: 15:42
 */
public class LineParser extends Parser {

    CodeParser code;

    int linum=0;

    LineParser(Writer out) {
        super(out);

        code = new CodeParser(out);
    }

    final Pattern LINE = Pattern.compile("(\\s{6}|\\t)(.*)");
    final Pattern CONT = Pattern.compile("(\\s{5}\\S)(.*)");
    final Pattern LABEL = Pattern.compile("(\\d[\\d\\s]{5})(.*)");
    final Pattern COMMENT = Pattern.compile("\\S(.*)");
    final Pattern EMPTY = Pattern.compile("\\s{0,5}");

    Parser parser =
            parser(EMPTY, m->{code.nl(); return true;})
            .or(parser(LINE, m->{
                code.nl();
                out.text("\t");
                parseCode(m.group(2));
                return true;
            })).or(parser(CONT, m->{
                out.nl().text("\t\t");
                parseCode(m.group(2));
                return true;
            })).or(parser(LABEL, m->{
                code.nl();
                out.text("l", m.group(1).trim());
                parseCode(m.group(2));
                return true;
            })).or(parser(COMMENT, m->{
                code.nl();
                String c = m.group(1);
                if(!c.trim().isEmpty())
                    out.text("c", c);
                return true;
            }));


    public boolean parse(String line) {

        ++linum;

        if(!parser.parse(line)) {
            out.text("x", line);
        }

        return true;
    }


    void parseCode(String line) {

        int c = line.lastIndexOf('!');

        code.parse(c<0 ? line.toLowerCase() : line.substring(0, c).toLowerCase());

        if(c>=0)
            out.text("c", line.substring(c+1));
    }

    public void close() {
        code.close();
    }
}
