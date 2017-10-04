package de.dst.fortran.lexer.lines;

import de.dst.fortran.lexer.token.Label;
import de.dst.fortran.lexer.token.Token;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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

    Integer label() {
        if(!lead.isEmpty() && Character.isDigit(lead.charAt(0)))
            return Integer.parseInt(lead);
        else
            return null;
    }
    
    static final Pattern pattern = Pattern.compile("\\s{6}|\\t|\\d+", Pattern.CASE_INSENSITIVE);

    static CodeLine match(String line) {
        Matcher m = pattern.matcher(line);
        if(m.lookingAt()) {
            String label = m.group();
            String code = line.substring(m.end());
            return new StartLine(line, label, code);
        }

        return null;
    }

    @Override
    public Stream<Token> stream() {
        Stream<Token> tokens = super.stream();

        Integer label = label();
        if(label!=null) {
            tokens = Stream.concat(Stream.of(new Label(label)), tokens);
        }

        return tokens;
    }
}
