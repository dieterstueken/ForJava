package de.dst.fortran.lexer.token;

import de.dst.fortran.lexer.LineBuffer;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 15:15
 */
public interface Toker<T extends Token> {
    T match(LineBuffer line);

    List<Toker> TOKERS = Arrays.asList(
            Space.toker,
            
            Function.toker,
            Subroutine.toker,
            BlockData.toker,
            Return.toker,

            Logical.toker,
            Sep.toker,
            Range.toker,
            
            Format.toker,
            FmtOpen.toker,
            FmtClose.toker,
            
            Common.toker,
            Dim.toker,
            Data.toker,
            
            If.toker,
            Then.toker,
            ElseIf.toker,
            Else.toker,
            EndIf.toker,
            
            Do.toker,
            DoWhile.toker,
            EndDo.toker,
            
            Goto.toker,
            Call.toker,
            Continue.toker,
            
            Fopen.toker,
            Fclose.toker,
            Frw.toker,
            Fmt.toker,
            Fprint.toker,
            Fstat.toker,
            Fmtrep.toker,
            
            Text.toker,
            Const.toker,
            Boolean.toker,
            Assign.toker,
            
            End.toker,
            Apply.toker,
            Name.toker,
            Binop.toker,

            Open.toker,
            Close.toker,

            Comment.toker
    );

    static Token token(LineBuffer line) {
        for (Toker toker : TOKERS) {
            Token token = toker.match(line);
            if (token != null)
                return token;
        }

        return null;
    }

    static void tokenize(LineBuffer line, Consumer<? super Token> tokens) {
        while(!line.isEmpty()) {
            Token token = token(line);
            if(token!=null) {
                tokens.accept(token);
            } else {
                token = token(line);
                throw new RuntimeException(line.toString());
            }
        }
    }

    static Stream<Token> stream(String line) {
        Stream.Builder<Token> builder = Stream.builder();
        tokenize(new LineBuffer(line), builder);
        return builder.build();
    }
}
