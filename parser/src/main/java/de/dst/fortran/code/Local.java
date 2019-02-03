package de.dst.fortran.code;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.01.19
 * Time: 21:12
 */

/**
 * Some local variable with a status.
 *
 * A local variable may be assigned, read and modified.
 * A variable may also be known but unused within this context.
 */
public class Local {

    public enum Stat {
        U, M, A, AM, R, RM, RA, RAM;

        static final List<Stat> STATS = List.of(Stat.values());

        boolean is(Stat m) { return (ordinal() & m.ordinal()) != 0; }

        public Stat read() {
            return STATS.get(ordinal() | R.ordinal());
        }

        public Stat write() {
            if(this==U) // initial assignment
                return A;
            else // modified still unread
                return STATS.get(ordinal() | M.ordinal() & ~R.ordinal());
        }
    }

    public static Stat stat(String stat) {
        if(stat==null || stat.isEmpty())
            return Stat.U;

        return Stat.valueOf(stat);
    }

    public final String name;

    public Stat stat;

    public Local(String name) {
        this(name, Stat.U);
    }

    public Local(String name, Stat stat) {
        this.name = name;
        this.stat = stat;
    }

    public Local(String name, String stat) {
         this(name, stat(stat));
    }

    public Stat getStat() {
        return stat;
    }

    public boolean isUnused() {return stat == Stat.U;}

    public boolean isRead() {return stat.is(Stat.R);}

    public boolean isAssigned() {return stat.is(Stat.A);}

    public boolean isExpected() {return !isUnused() && !isAssigned();}

    public boolean isModified() {return stat.is(Stat.M);}

    public Local read() {
        stat = stat.read();
        return this;
    }

    public Local write() {
        stat = stat.write();
        return this;
    }

    public Local copy() {
        return new Local(name, stat);
    }

    public String toString() {
        return name + ": " + stat;
    }
}
