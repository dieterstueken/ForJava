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

    public String toString() {
        if(toString==null) {
            StringBuffer buffer = new StringBuffer();

            if (type != null)
                buffer.append(type);
            else
                buffer.append(isInteger() ? "I" : "R");
            buffer.append(" ");

            if (context != null)
                buffer.append(context.getName()).append(".");

            buffer.append(name);
            if (dim != null) {
                buffer.append("(");
                for (int i = 0; i < dim.size() - 1; ++i)
                    buffer.append(",");
                buffer.append(")");
            }

            toString = buffer.toString();
        }

        return toString;
    }

    public boolean isInteger() {
        return "ijklmn".indexOf(name.charAt(0))>=0;
    }

    public Variable context(Context context) {

        if(this.context!=null)
            throw new RuntimeException("duplicate context");

        this.context = context;
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
        return this;
    }

    public Variable type(Type type) {
        this.type = type;
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
