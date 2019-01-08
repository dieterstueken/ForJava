package de.dst.fortran.code;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  07.02.13 18:48
 * modified by: $Author$
 * modified on: $Date$
 */
public class CommonAnalyzer extends Entity implements Common {

    public CommonAnalyzer root;

    final Function<String, Variable> newVariable;

    final Entities<Variable> members;

    public CommonAnalyzer(String common, Function<String, Variable> newVariable) {
        super(common);
        this.newVariable = newVariable;

        members = new Entities<>(this::newVariable);
    }

    @Override
    public Stream<Variable> members() {
        return members.stream();
    }

    Variable newVariable(String name) {
        Variable variable = newVariable.apply(name).context(this);

        // always by reference
        //variable.isReferenced(true);

        if(root!=null) {
            int index = members.size();
            Variable ref = root.members.get(index);
            variable.ref = ref;
            if(!ref.name.equals(variable.name)) {
                System.out.format("alias /%s/ %s : %s\n", name, variable.name, ref.name);
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
        if (!(o instanceof CommonAnalyzer)) return false;
        if (!super.equals(o)) return false;

        CommonAnalyzer common = (CommonAnalyzer) o;

        return members.equals(common.members);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + members.hashCode();
        return result;
    }
}
