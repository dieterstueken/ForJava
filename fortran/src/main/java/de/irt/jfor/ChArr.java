package de.irt.jfor;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  19.10.2017 14:17
 * modified by: $Author$
 * modified on: $Date$
 */
public class ChArr implements Arr {

    public final String text;

    public ChArr(int len) {
        text = "";
    }

    public ChArr(String value) {
        text = value;
    }

    public ChArr() {
        text = "";
    }

    public String toString() {
        return text.toString();
    }

    public char get(int index) {
        return text.charAt(index);
    }

    public static ChArr of() {
        return new ChArr();
    }

    public static ChArr of(int len) {
        return new ChArr(len);
    }

    public ChArr cat(String text) {
        return new ChArr(this.text+text);
    }

    public ChArr get(int i, int len) {
        len = Math.min(len, text.length());
        return new ChArr(toString().substring(i-1, i+len));
    }

    public int len_trim() {
        int i = text.length();
        while(i>0) {
            if(text.charAt(i-1)!=' ')
                break;
            --i;
        }
        return i;
    }
}
