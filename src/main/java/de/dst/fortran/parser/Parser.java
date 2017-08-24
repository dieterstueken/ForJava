package de.dst.fortran.parser;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 08.02.13
 * Time: 18:49
 */
interface Parser  {

    /**
     * Parse a given line and return remaining text or null.
     * May return the whole line again.
     * @param line to parse.
     * @return any remaining text unable to parse or null
     */
    String parse(String line);

    default Parser or(Parser other) {
        return new Parsers(this, other);
    }
}
