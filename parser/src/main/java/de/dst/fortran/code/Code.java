package de.dst.fortran.code;

import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.04.17
 * Time: 13:44
 */
public class Code extends Entity {

    public final String path;

    final String unitType;

    final Type returnType;

    public Code(String path, Element be) {
        super(be.getAttribute("name"));
        this.path = path;
        unitType = be.getNodeName();

        if(unitType.equals("function"))
            returnType = Type.parse(be.getAttribute("type"), name);
        else
            returnType = Type.NONE;
    }

    public Type getReturnType() {
        return this.returnType;
    }

    public final Entities<Variable> variables = new Entities<>(Variable::new);

    public final Entities<Variable> arguments = new Entities<>(name -> variables.get(name).context(this).isArgument(true));

    public final Set<String> functions = new HashSet<>();

    public final Set<Code> blocks = new HashSet<>();

    public final Entities<CommonAnalyzer> commons = new Entities<>(this::newCommon);

    private CommonAnalyzer newCommon(String name) {
        return new CommonAnalyzer(name, variables::get);
    }

    public void dump() {

        System.out.format("%s %s\n", name, unitType);

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
