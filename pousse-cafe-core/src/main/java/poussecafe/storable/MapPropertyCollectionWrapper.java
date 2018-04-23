package poussecafe.storable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static poussecafe.check.Checks.checkThatValue;

public abstract class MapPropertyCollectionWrapper<K, D, E extends D> implements MapProperty<K, D> {

    public MapPropertyCollectionWrapper(Collection<E> collection) {
        checkThatValue(collection).notNull();
        this.wrappedCollection = collection;
        buildMap();
    }

    private Collection<E> wrappedCollection;

    private void buildMap() {
        map = new HashMap<>();
        for(D data : wrappedCollection) {
            map.put(extractKey(data), data);
        }
    }

    private Map<K, D> map;

    protected abstract K extractKey(D data);

    @Override
    public Map<K, D> get() {
        return Collections.unmodifiableMap(map);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void set(Map<K, D> value) {
        wrappedCollection.clear();
        for(D data : value.values()) {
            wrappedCollection.add((E) data);
        }
        buildMap();
    }

    @Override
    public Optional<D> get(K key) {
        if(map.containsKey(key)) {
            return Optional.of(map.get(key));
        } else {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void put(K key,
            D value) {
        if(map.containsKey(key)) {
            remove(key);
        }
        K extractedKey = extractKey(value);
        if(!extractedKey.equals(key)) {
            throw new IllegalArgumentException("Key does not match value");
        }
        map.put(key, value);
        wrappedCollection.add((E) value);
    }

    @Override
    public Collection<D> values() {
        return Collections.unmodifiableCollection(wrappedCollection);
    }

    @Override
    public D remove(K key) {
        if(map.containsKey(key)) {
            D removedData = map.remove(key);
            wrappedCollection.remove(removedData);
            return removedData;
        } else {
            return null;
        }
    }

    @Override
    public boolean isEmpty() {
        return wrappedCollection.isEmpty();
    }

}