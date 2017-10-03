package de.dst.fortran.lexer.token;

import org.intellij.lang.annotations.Language;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 11:10
 */
public class Token {

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    static <T extends Token> Toker<T> toker(@Language("regexp") String regex, Function<Matcher, T> token) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return (line) -> {
            Matcher m = pattern.matcher(line.toString());
            if(!m.lookingAt())
                return null;

            line.eat(m.end());
            return token.apply(m);
        };
    }
}
