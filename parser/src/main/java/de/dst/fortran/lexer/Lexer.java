package de.dst.fortran.lexer;

import de.dst.fortran.lexer.lines.Line;

import java.io.File;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.09.17
 * Time: 11:17
 */
abstract public class Lexer {

  public static void main(String ... args) {

    Stream.of(args).map(File::new)
            .peek(System.out::println)
            .flatMap(Line::lines)
            .map(Line::matchLine)
            .flatMap(Line::stream)
            .forEach(System.out::println);
  }
}
