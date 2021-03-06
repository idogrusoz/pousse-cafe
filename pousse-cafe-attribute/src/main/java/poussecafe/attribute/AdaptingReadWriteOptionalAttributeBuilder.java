package poussecafe.attribute;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @param <U> Stored type
 * @param <T> Attribute type
 */
public class AdaptingReadWriteOptionalAttributeBuilder<U, T> {

    AdaptingReadWriteOptionalAttributeBuilder(Supplier<T> getter, Function<T, U> adapter) {
        this.getter = getter;
        this.adapter = adapter;
    }

    private Supplier<T> getter;

    private Function<T, U> adapter;

    public ReadWriteOptionalAttributeBuilder<T> write(Consumer<U> setter) {
        Objects.requireNonNull(setter);

        CompositeAttribute<T, T> compositeAttribute = new CompositeAttribute<>();
        compositeAttribute.getter = getter;
        compositeAttribute.setter = value -> {
            if(value != null) {
                setter.accept(adapter.apply(value));
            } else {
                setter.accept(null);
            }
        };
        return new ReadWriteOptionalAttributeBuilder<>(compositeAttribute);
    }
}
