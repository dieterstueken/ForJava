package de.dst.fortran.code;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 16.02.19
 * Time: 12:53
 */
public class Locals {

    private final Map<String, VStat> vars = new TreeMap<>();

    public boolean isEmpty() {
        return vars.isEmpty();
    }

    public VStat get(String name) {
        return vars.get(name);
    }

    public boolean isKnown(String name) {
        return vars.containsKey(name);
    }

    public boolean contains(Map.Entry<String, VStat> entry) {
        VStat stat = vars.get(entry.getKey());
        return entry.getValue().equals(stat);
    }

    public void put(String name, VStat stat) {
        vars.put(name, stat);
    }

    public void putAll(Locals other) {
        vars.putAll(other.vars);
    }

    public Set<Map.Entry<String, VStat>> entries() {
        return vars.entrySet();
    }

    public void forEach(BiConsumer<String, VStat> action) {
        vars.forEach(action);
    }

    public Set<String> getNames() {
        return vars.keySet();
    }

    static VStat read(String key, VStat stat) {
        if (stat == null)
            return define(key, VStat.R);
        else
            return stat.read();
    }

    static VStat write(String key, VStat stat) {
        if (stat == null)
            return define(key, VStat.A);
        else
            return stat.write();
    }

    static VStat define(String key, VStat stat) {
        return stat;
    }

    // get current or undefined
    public VStat define(String name) {
        return vars.computeIfAbsent(name, k -> define(k, VStat.U));
    }

    public VStat read(String name) {
        return vars.compute(name, Locals::read);
    }

    public VStat write(String name) {
        return vars.compute(name, Locals::write);
    }

    // apply locals to the enclosing context
    public void applyTo(Locals context) {
        vars.forEach((name, stat)->{
            VStat cstat = context.define(name);     // x -> U

            // propagate changes
            if(cstat!=stat) {
                if (stat.isExpected())
                    context.read(name);

                if (stat.isProvided() | stat.isModified())
                    context.write(name);
            }
        });
    }
}
