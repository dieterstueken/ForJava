package de.dst.fortran.code;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.04.17
 * Time: 13:44
 */
public class Block extends Entity implements Context {

    public String path = null;

    public String type = null;

    public Type returnType = null;

    public final Entities<Variable> variables = new Entities<>(Variable::new);

    public final Entities<Variable> arguments = new Entities<>(name -> variables.get(name).context(this));

    public final Set<String> assigned = new HashSet<>();

    public final Set<String> functions = new HashSet<>();

    public final Set<Block> blocks = new HashSet<>();

    public final Entities<Common> commons = new Entities<>(this::newCommon);

    public Block(String name) {
        super(name);
    }

    private Common newCommon(String name) {
        return new Common(name, variables::get);
    }

    public Variable assign(Variable v) {
        assigned.add(v.name);
        return v;
    }

    public boolean assigned(Variable v) {
        return assigned.contains(v.name);
    }

    public void dump() {

        System.out.format("%s %s\n", name, type);

        arguments.forEach(v ->
                System.out.format("%s %s\n", assigned(v) ? "*":" ", v)
        );

        variables.forEach(v -> {
            if(v.context==null)
                System.out.format("%s %s\n", assigned(v) ? "*":" ", v);
        });

        System.out.println("--");
    }

}
