package de.dst.fortran.code;

import static de.dst.fortran.code.Local.Stat.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.01.19
 * Time: 21:12
 */
public class Local {

    public enum Stat {USE, EXP, ASS, USM, EXM, MOD}

    public static Stat stat(String stat) {
        if(stat==null || stat.isEmpty())
            return USE;

        return Stat.valueOf(stat);
    }

    public final String name;

    Stat stat;

    public Local(String name) {
        this(name, USE);
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

    public boolean isExpected() {
        return stat == EXP || stat==EXM;
    }

    public boolean isAssigned() {
        return stat == ASS || stat==MOD;
    }

    public boolean isModified() {
        return stat == MOD || stat==EXM || stat == USM;
    }

    public Stat read() {
        if(stat==Stat.USE)
            stat = EXP;
        return stat;
    }

    public Stat modified() {
        switch(stat) {
            case USE:
                stat = USM;
                break;

            case EXP:
                stat = EXM;
                break;

            case ASS:
                 stat = MOD;
                 break;

            case USM:
            case MOD:
            case EXM:
                break;
        }

        return stat;
    }

    public Stat write() {
        switch(stat) {
            case USE:
                stat = ASS;
                break;

            case EXP:
                stat = EXM;
                break;

            case ASS:
                stat = MOD;
                break;

            case USM:
            case MOD:
            case EXM:
                break;
        }

        return stat;
    }
}
