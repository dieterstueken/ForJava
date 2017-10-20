package de.irt.jfor;

import java.util.ArrayList;
import java.util.List;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 14:17
 * modified by: $Author$
 * modified on: $Date$
 */
public class StringArr implements Arr {

    public final List<String> strings;

    public StringArr(int len) {
        strings = new ArrayList<>(len);
        for(int i=0; i<len; ++i)
            strings.add("");
    }

    public String get(int index) {
        return strings.get(index-1);
    }

    public void set(int index, String s) {
        strings.set(index-1, s);
    }

    public String toString() {
        return "string[]";
    }

    public static StringArr of(int len) {
        return new StringArr(len);
    }
}
