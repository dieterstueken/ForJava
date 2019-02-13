package de.dst.fortran.code;

import java.util.List;

/**
 * A local variable status.
 *
 * A local variable may be assigned, read and modified.
 * A variable may also be known but unused within this context.
 */
public enum LStat {
    U, M, A, AM, R, RM, RA, RAM;

    static final List<LStat> STATS = List.of(LStat.values());

    boolean is(LStat m) {
        return (ordinal() & m.ordinal()) != 0;
    }

    public LStat read() {
        return STATS.get(ordinal() | R.ordinal());
    }

    public LStat write() {
        if (this == U) // initial assignment
            return A;
        else // modified still unread
            return STATS.get(ordinal() | M.ordinal() & ~R.ordinal());
    }

    public boolean isUnused() {return this == LStat.U;}

    public boolean isRead() {return this.is(LStat.R);}

    public boolean isAssigned() {return this.is(LStat.A);}

    // used unassigned
    public boolean isExpected() {return !isUnused() && !isAssigned();}

    // left unead
    public boolean isProvided() {return !isUnused() && !isRead();}

    public boolean isModified() {return this.is(LStat.M);}
}
