package de.dst.fortran.code;

import java.util.*;

import static de.dst.fortran.code.Type.I4;
import static de.dst.fortran.code.Variable.Prop.*;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  07.02.13 18:52
 * modified by: $Author$
 * modified on: $Date$
 */
public class Variable extends Entity implements Value {

    public enum Prop {EXPLICIT, ARGUMENT, ASSIGNED, MODIFIED, REFERENCED, RETURNED, INDEX, ALLOCATABLE}

    public static final List<String> INDEX_NAMES = List.of("ni", "nj", "nk");

    public final Set<Prop> props = EnumSet.noneOf(Prop.class);

    Type type = null;

    public List<Value> dim = Collections.emptyList();

    public Context context = null;

    public Variable ref;

    transient String toString;

    public Variable(String name) {
        super(name);
    }

    public Variable(String name, Context context) {
        super(name);
        this.context = context;
    }

    public boolean isArray() {
        if(!dim.isEmpty())
            return true;

        //if(type!=null && Arr.class.isAssignableFrom(type.type()))
        //    return true;

        if(Type.STR.equals(type)) {
            // strings may be treated as arrays
            return true;
        }

        return false;
    }

    public boolean isPrimitive() {
        if(isArray())
            return false;

        if(isReferenced())
            return false;

        if(isArgument() && isAssigned())
            return false;

        if(type==Type.CPX)
            return false;

        return true;
    }

    public boolean isLocal() {
        return context==null && (type==null || isPrimitive()) && !isReferenced();
    }

    public boolean isModified() {
        return props.contains(MODIFIED);
    }

    public boolean isIndex() {
        return props.contains(INDEX);
    }

    // was used to allocate an array
    public boolean wasIndex() {
        return isIndex() || (ref!=null && ref.isIndex());
    }

    public int getIndex() {
        if(isIndex() && ref!=null)
            return ref.dim.indexOf(this);
        return -1;
    }

    public TypeDef typeDef() {
        Type type = this.type != null ? this.type : Type.intrinsic(name);

        if(isPrimitive()) {
            return type.primitive();
        }

        TypeDef tmp = type.dim(dim.size());
        return tmp;
    }

    public Type getType() {
        return typeDef().getType();
    }

    public boolean isInt() {
        return getType().isInt();
    }

    public boolean isReal() {
        return getType().isReal();
    }

    public boolean isCpx() {
        return getType().equals(Type.CPX);
    }

    public String getRefName() {
        return ref!=null ? ref.getName() : getName();
    }

    public String toString() {
        if(context == null && dim.isEmpty())
            return name;

        if(toString==null) {
            StringBuffer buffer = new StringBuffer();

            buffer.append(typeDef());
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
        if(assigned) {
            if(isIndex()) {
                //System.err.format("index assignment: %s\n", name);
                throw new IllegalStateException("assign to index");
            }
            
            if (!props.add(Prop.ASSIGNED))
                props.add(MODIFIED);
        }

        return this;
    }

    public boolean isAssigned() {
        return props.contains(Prop.ASSIGNED);
    }

    public Variable setReferenced() {
        props.add(Prop.REFERENCED);
        if(ref!=null)
            ref.setReferenced();

        return this;
    }

    public boolean isReferenced() {
        return props.contains(Prop.REFERENCED);
    }

    // add dimension
    public Variable dim(Value dim) {
        if(this.dim==null || this.dim.isEmpty())
            this.dim=new ArrayList<>();

        return dim(this.dim.size(), dim);
    }

    // set/allocate dimension
    public Variable dim(int i, Value dim) {

        if(i<this.dim.size())
            this.dim.set(i, dim);
        else
            this.dim.add(dim);

        if(dim instanceof Variable) {
            ((Variable)dim).asIndexOf(this, i);
        }

        this.toString = null;

        return this;
    }

    // setup index reference
    void asIndexOf(Variable arr, int dim) {
        if(ref!=null) {
            // is already index ...
            if(!ref.isIndex()) // should be an array, too
                throw new IllegalArgumentException("dimension mismatch)");
        } else {
            String name = INDEX_NAMES.get(dim);
            this.ref = new Variable(name, arr).prop(INDEX).type(I4);

            // allocatable index is not propagated
            // this will stay a normal variable which was used to dimension an array
            if(!arr. props.contains(ALLOCATABLE))
                this.prop(INDEX);
        }
    }

    public Variable type(Type type) {
        this.type = type;
        this.toString = null;
        return this;
    }

    public Variable prop(Prop prop) {
        props.add(prop);
        this.toString = null;
        return this;
    }

    public Variable decl(Type type) {
        return type(type).prop(Prop.EXPLICIT);
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
        result = 31 * result + Objects.hashCode(type);
        result = 31 * result + dim.hashCode();
        result = 31 * result + Objects.hashCode(context);
        return result;
    }
}
