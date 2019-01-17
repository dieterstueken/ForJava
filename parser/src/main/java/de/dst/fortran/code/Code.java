package de.dst.fortran.code;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.04.17
 * Time: 13:44
 */
public class Code extends Entity {

    public String path = null;

    String type = null;

    TypeDef returnType = null;

    public TypeDef type() {
        if(!this.type.equals("function"))
            return null;  // subroutine

        return this.returnType != null ? this.returnType : Type.intrinsic(name).primitive();
    }

    public final Entities<Variable> variables = new Entities<>(Variable::new);

    public final Entities<Variable> arguments = new Entities<>(name -> variables.get(name).context(this).isArgument(true));

    public final Set<String> functions = new HashSet<>();

    public final Set<Code> blocks = new HashSet<>();

    public final Entities<CommonAnalyzer> commons = new Entities<>(this::newCommon);

    public Code(String name) {
        super(name);
    }

    private CommonAnalyzer newCommon(String name) {
        return new CommonAnalyzer(name, variables::get);
    }

    public void dump() {

        System.out.format("%s %s\n", name, type);

        arguments.forEach(v ->
                System.out.format("%s %s\n", v.isAssigned() ? "*":" ", v)
        );

        variables.forEach(v -> {
            if(v.context==null)
                System.out.format("%s %s\n", v.isAssigned() ? "*":" ", v);
        });

        System.out.println("--");
    }

}
