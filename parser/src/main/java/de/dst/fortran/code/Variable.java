package de.dst.fortran.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  07.02.13 18:52
 * modified by: $Author$
 * modified on: $Date$
 */
public class Variable extends Entity implements Value, Context {

    public Type type = null;

    public List<Value> dim = null;

    public Context context = null;

    transient String toString;

    public Variable(String name) {
        this(name, null);
    }

    public Type type() {
        return type != null ? type : Type.intrinsic(name);
    }

    public String toString() {
        if(context == null && dim == null)
            return name;

        if(toString==null) {
            StringBuffer buffer = new StringBuffer();

            buffer.append(type());
            buffer.append(" ");

            if (context != null)
                buffer.append(context.getName()).append(".");

            buffer.append(name);

            if (dim != null) {
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

    public Variable(String name, Context context) {
        super(name);
        this.context = context;
    }

    public Variable dim(Value dim) {
        if(this.dim==null)
            this.dim=new ArrayList<>();
        this.dim.add(dim);
        this.toString = null;

        return this;
    }

    public Variable type(Type type) {
        this.type = type;
        this.toString = null;

        return this;
    }

    public List<Value> dim() {
        return this.dim==null ? Collections.emptyList() : dim;
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
                && Objects.equals(dim(), variable.dim())
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
