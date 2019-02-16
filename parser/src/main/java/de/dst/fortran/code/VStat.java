package de.dst.fortran.code;

import java.util.List;

/**
 * A local variable status.
 *
 * A local variable may be assigned, read and modified.
 * A variable may also be known but unused within this context.
 */
public enum VStat {
    U, M, A, AM, R, RM, RA, RAM;

    static final List<VStat> STATS = List.of(VStat.values());

    public static VStat of(String name) {
        return name==null || name.isEmpty() ? U : valueOf(name);
    }

    boolean is(VStat s) {
        return (ordinal() & s.ordinal()) != 0;
    }
    VStat set(VStat s) {
        return STATS.get(ordinal() | s.ordinal());
    }
    VStat clr(VStat s) {
        return STATS.get(ordinal() & ~s.ordinal());
    }

    public boolean isUnused() {return this == VStat.U;}
    public boolean isAssigned() {return this.is(VStat.A);}
    public boolean isModified() {return this.is(VStat.M);}
    public boolean isRead() {return this.is(VStat.R);}

    // used unassigned
    public boolean isExpected() {return !isUnused() && !isAssigned();}

    // left unread
    public boolean isProvided() {return !isUnused() && !isRead();}

    // modifications

    public VStat read() {
        return STATS.get(ordinal() | R.ordinal());
    }

    public static VStat read(VStat s) {
        return s==null ? VStat.R : s.read();
    }

    public VStat write() {
        return set(M).clr(R);
    }

    public static VStat write(VStat s) {
        return s==null ? VStat.A : s.write();
    }
}
