package de.dst.fortran.code;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 16.02.19
 * Time: 12:53
 */
public class Locals {

    private final Map<String, VStat> vars = new HashMap<>();

    public boolean isEmpty() {
        return vars.isEmpty();
    }

    public VStat get(String name) {
        return vars.get(name);
    }

    public boolean isKnown(String name) {
        return vars.containsKey(name);
    }

    public void put(String name, VStat stat) {
        vars.put(name, stat);
    }

    public void putAll(Locals other) {
        vars.putAll(other.vars);
    }

    public void forEach(BiConsumer<String, VStat> action) {
        vars.forEach(action);
    }

    public Set<String> getNames() {
        return vars.keySet();
    }

    // get current or undefined
    public VStat define(String name) {
        return vars.computeIfAbsent(name, k -> VStat.U);
    }

    public VStat read(String name) {
        return vars.compute(name, (k, s) -> VStat.read(s));
    }

    public VStat write(String name) {
        return vars.compute(name, (k, s) -> VStat.write(s));
    }

    // apply locals to the enclosing context
    public void applyTo(Locals context) {
        vars.forEach((name, stat)->{
            VStat cstat = context.define(name);     // x -> U

            // propagate changes
            if(cstat!=stat) {
                if (stat.isExpected())
                    context.read(name);

                if (stat.isProvided())
                    context.write(name);
            }
        });
    }
}
