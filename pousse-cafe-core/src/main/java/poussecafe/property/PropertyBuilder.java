package poussecafe.property;

import poussecafe.domain.Entity;

import static poussecafe.check.Checks.checkThatValue;

public class PropertyBuilder {

    private PropertyBuilder() {

    }

    public static <T> SimplePropertyBuilder<T> simple(Class<T> valueClass) { // NOSONAR
        return new SimplePropertyBuilder<>();
    }

    public static <T> ListPropertyBuilder<T> list(Class<T> elementClass) { // NOSONAR
        return new ListPropertyBuilder<>();
    }

    public static <T> SetPropertyBuilder<T> set(Class<T> elementClass) { // NOSONAR
        return new SetPropertyBuilder<>();
    }

    public static <K, V> MapPropertyBuilder<K, V> map(Class<K> keyClass, Class<V> valueClass) {
        checkThatValue(keyClass).notNull();
        checkThatValue(valueClass).notNull();
        return new MapPropertyBuilder<>();
    }

    public static <T> OptionalPropertyBuilder<T> optional(Class<T> valueClass) { // NOSONAR
        return new OptionalPropertyBuilder<>();
    }

    public static <T extends Number> NumberPropertyBuilder<T> number(Class<T> valueClass) { // NOSONAR
        return new NumberPropertyBuilder<>();
    }

    public static <K, E extends Entity<K, ?>> EntityMapPropertyBuilder<K, E> entityMap(Class<K> entityKeyClass, Class<E> entityClass) {
        checkThatValue(entityKeyClass).notNull();
        checkThatValue(entityClass).notNull();
        return new EntityMapPropertyBuilder<>(entityClass);
    }

    public static IntegerPropertyBuilder integer() {
        return new IntegerPropertyBuilder();
    }
}
