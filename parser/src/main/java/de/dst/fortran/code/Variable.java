package de.dst.fortran.code;

import java.util.*;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  07.02.13 18:52
 * modified by: $Author$
 * modified on: $Date$
 */
public class Variable extends Entity implements Value, Context {

    enum Prop {EXPLICIT, ARGUMENT, ASSIGNED, REFERENCED};

    public final Set<Prop> props = EnumSet.noneOf(Prop.class);

    public Type type = null;

    public List<Value> dim = Collections.emptyList();

    public Context context = null;

    public Variable alias;

    transient String toString;

    public Variable(String name) {
        super(name);
    }

    public Variable(String name, Context context) {
        super(name);
        this.context = context;
    }

    public boolean isPrimitive() {
        if(!dim.isEmpty())
            return false;

        if(isReferenced())
            return false;

        if(isArgument() && isAssigned())
            return false;

        return true;
    }

    public Class<?> type() {
        Type type = this.type != null ? this.type : Type.intrinsic(name);

        if(isPrimitive() && type.simple!=null) {
            return type.simple;
        }

        return type.type(dim.size());
    }

    public String toString() {
        if(context == null && dim.isEmpty())
            return name;

        if(toString==null) {
            StringBuffer buffer = new StringBuffer();

            buffer.append(type());
            buffer.append(" ");

            if (context != null)
                buffer.append(context.getName()).append(".");

            buffer.append(name);

            if (!dim.isEmpty()) {
                char sep = '(';
                for (Value iv : dim) {
                    String name = iv instanceof Entity ? ((Entity)iv).getName() : iv.toString();
                    buffer.append(sep).append(name);
                    sep = ',';
                }
                buffer.append(")");
            }

            toString = buffer.toString();
        }

        return toString;
    }

    public Variable context(Context context) {

        if(this.context!=null)
            throw new RuntimeException("duplicate context");

        this.context = context;
        this.toString = null;
        return this;
    }


    public Variable isArgument(boolean argument) {
        if(argument)
            props.add(Prop.ARGUMENT);
        return this;
    }

    public boolean isArgument() {
        return props.contains(Prop.ARGUMENT);
    }

    public Variable isAssigned(boolean assigned) {
        if(assigned)
            props.add(Prop.ASSIGNED);
        return this;
    }

    public boolean isAssigned() {
        return props.contains(Prop.ASSIGNED);
    }

    public Variable isReferenced(boolean referenced) {
        if(referenced)
            props.add(Prop.REFERENCED);
        return this;
    }

    public boolean isReferenced() {
        return props.contains(Prop.REFERENCED);
    }

    public Variable dim(Value dim) {
        if(this.dim==null || this.dim.isEmpty())
            this.dim=new ArrayList<>();

        this.dim.add(dim);
        this.toString = null;

        return this;
    }

    public Variable type(Type type) {
        this.type = type;
        this.toString = null;
        props.add(Prop.EXPLICIT);
        return this;
    }

    public String contextName() {
        return context==null ? "" : context.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Variable)) return false;
        if (!super.equals(o)) return false;

        Variable variable = (Variable) o;

        boolean eq = Objects.equals(type, variable.type)
                && Objects.equals(dim, variable.dim)
                && Objects.equals(contextName(), variable.contextName());

        return eq;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + dim.hashCode();
        result = 31 * result + context.hashCode();
        return result;
    }
}
