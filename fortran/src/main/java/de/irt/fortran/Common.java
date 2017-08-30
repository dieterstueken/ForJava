package de.irt.fortran;

import java.util.function.Supplier;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.08.17
 * Time: 16:30
 */
public class Common {

    public static class Builder<C extends Common> {
        final Class<C> type;
        final Supplier<C> create;

        public Builder(Class<C> type, Supplier<C> create) {
            this.type = type;
            this.create = create;
        }

        public C create() {
            return create.get();
        }
    }
}
