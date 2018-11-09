package poussecafe.property;

public class ReadWriteIntegerPropertyBuilder {

    public ReadWriteIntegerPropertyBuilder(CompositeProperty<Integer, Integer> compositeProperty) {
        this.compositeProperty = compositeProperty;
    }

    private CompositeProperty<Integer, Integer> compositeProperty;

    public IntegerProperty build() {
        return new IntegerProperty() {
            @Override
            public Integer get() {
                return compositeProperty.getter.get();
            }

            @Override
            public void set(Integer value) {
                compositeProperty.setter.accept(value);
            }
        };
    }
}
