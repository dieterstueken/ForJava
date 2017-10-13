package de.dst.fortran.code;

import java.util.function.Function;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  07.02.13 18:48
 * modified by: $Author$
 * modified on: $Date$
 */
public class Common extends Entity implements Context {

    public Common root;

    public final Entities<Variable> members;

    final Function<String, Variable> newVariable;

    public Common(String common, Function<String, Variable> newVariable) {
        super(common);
        this.newVariable = newVariable;

        members = new Entities<>(this::newVariable);
    }

    Variable newVariable(String name) {
        Variable variable = newVariable.apply(name).context(this);

        if(root!=null) {
            int index = members.size();
            Variable alias = root.members.get(index);
            if(!alias.name.equals(variable.name)) {
                variable.alias = alias;
                System.out.format("alias /%s/ %s : %s\n", name, variable.name, alias.name);
            }
        }

        return variable;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append('/').append(name).append(' ');
        char sep='/';
        for (Variable m : members) {
            buffer.append(sep).append(m.name);
            sep = ',';
        }

        return buffer.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Common)) return false;
        if (!super.equals(o)) return false;

        Common common = (Common) o;

        return members.equals(common.members);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + members.hashCode();
        return result;
    }
}
