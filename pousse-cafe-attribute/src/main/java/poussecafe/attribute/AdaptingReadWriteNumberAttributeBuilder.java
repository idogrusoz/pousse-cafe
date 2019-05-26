package poussecafe.attribute;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @param <U> Stored type
 * @param <T> Attribute type
 */
public class AdaptingReadWriteNumberAttributeBuilder<U, T extends Number> {

    AdaptingReadWriteNumberAttributeBuilder(Supplier<T> getter, Function<T, U> adapter) {
        this.getter = getter;
        this.adapter = adapter;
    }

    private Supplier<T> getter;

    private Function<T, U> adapter;

    public OperatorLessReadWriteNumberAttributeBuilder<T> write(Consumer<U> setter) {
        CompositeAttribute<T, T> compositeAttribute = new CompositeAttribute<>();
        compositeAttribute.getter = getter;
        compositeAttribute.setter = value -> setter.accept(adapter.apply(value));
        return new OperatorLessReadWriteNumberAttributeBuilder<>(compositeAttribute);
    }
}
