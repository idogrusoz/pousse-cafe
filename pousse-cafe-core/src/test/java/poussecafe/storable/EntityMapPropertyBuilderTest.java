package poussecafe.storable;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import poussecafe.domain.SimpleEntityData;
import poussecafe.util.ReflectionUtils;
import poussecafe.util.StringKey;

import static java.util.stream.Collectors.toMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EntityMapPropertyBuilderTest {

    private Map<String, SimpleEntityData> value = initValue();

    private Map<String, SimpleEntityData> initValue() {
        SimpleEntityData data = new SimpleEntityData();
        String keyString = "key";
        data.key().set(new StringKey(keyString));

        Map<String, SimpleEntityData> value = new HashMap<>();
        value.put(keyString, data);
        return value;
    }

    private Map<String, SimpleEntityData> newValue = initNewValue();

    private Map<String, SimpleEntityData> initNewValue() {
        SimpleEntityData data = new SimpleEntityData();
        String keyString = "key2";
        data.key().set(new StringKey(keyString));

        Map<String, SimpleEntityData> value = new HashMap<>();
        value.put(keyString, data);
        return value;
    }

    @Test
    public void readOnlyWithConversion() {
        givenReadOnlyPropertyWithConversion();
        whenReadingValueWithConversion();
        thenValueWithConvertionIs(value
                .entrySet()
                .stream()
                .collect(toMap(entry -> new StringKey(entry.getKey()), entry -> entity(entry.getValue()))));
    }

    private SimpleEntity entity(SimpleEntityData data) {
        SimpleEntity entity = new SimpleEntity();
        ReflectionUtils.access(entity).set("data", data);
        return entity;
    }

    private void givenReadOnlyPropertyWithConversion() {
        propertyWithConversion = PropertyBuilder.entityMap(StringKey.class, SimpleEntity.class)
                .from(String.class, SimpleEntityData.class)
                .adapt(StringKey::new)
                .read()
                .adapt(StringKey::getValue)
                .write()
                .build(value);
    }

    private EntityMapProperty<StringKey, SimpleEntity> propertyWithConversion;

    private void whenReadingValueWithConversion() {
        valueWithConversion = propertyWithConversion.inContextOf(primitive()).get();
    }

    private Primitive primitive() {
        return new Primitive() {
            @SuppressWarnings("unchecked")
            @Override
            <T> T newPrimitive(PrimitiveSpecification<T> specification) {
                if(specification.isWithData()) {
                    return (T) entity(new SimpleEntityData());
                } else if(specification.getExistingData() != null) {
                    return (T) entity((SimpleEntityData) specification.getExistingData());
                } else {
                    return (T) new SimpleEntity();
                }
            }
        };
    }

    private Map<StringKey, SimpleEntity> valueWithConversion;

    private void thenValueWithConvertionIs(Map<StringKey, SimpleEntity> value) {
        assertThat(valueWithConversion, is(value));
    }

    @Test
    public void readWriteWithConversion() {
        givenReadWritePropertyWithConversion();
        whenWritingValueWithConversion();
        thenValueWithConvertionIs(valueWithConversion);
    }

    private void givenReadWritePropertyWithConversion() {
        propertyWithConversion = PropertyBuilder.entityMap(StringKey.class, SimpleEntity.class)
                .from(String.class, SimpleEntityData.class)
                .adapt(StringKey::new)
                .read()
                .adapt(StringKey::getValue)
                .write()
                .build(value);
    }

    private void whenWritingValueWithConversion() {
        propertyWithConversion.inContextOf(primitive()).set(newValue
                .entrySet()
                .stream()
                .collect(toMap(entry -> new StringKey(entry.getKey()), entry -> entity(entry.getValue()))));
        valueWithConversion = propertyWithConversion.inContextOf(primitive()).get();
    }
}
