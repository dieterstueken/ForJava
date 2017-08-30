package de.irt.fortran;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.08.17
 * Time: 16:58
 */
public class IArr {

    private int values[];

    private IArr(int len) {
        values = new int[len];
    }

    public static IArr allocate(int len) {
        return new IArr(len);
    }

    public int len() {
        return values.length;
    }

    public int get(int index) {
        return values[index-1];
    }

    public int set(int index, int ival) {
        values[index-1] = ival;
        return ival;
    }
}
