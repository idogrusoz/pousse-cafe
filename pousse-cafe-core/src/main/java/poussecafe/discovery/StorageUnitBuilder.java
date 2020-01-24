package poussecafe.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import poussecafe.domain.EntityAttributes;
import poussecafe.domain.EntityDataAccess;
import poussecafe.environment.EntityImplementation;
import poussecafe.exception.PousseCafeException;
import poussecafe.storage.Storage;
import poussecafe.util.ReflectionUtils;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class StorageUnitBuilder {

    StorageUnitBuilder(Storage storage) {
        Objects.requireNonNull(storage);
        this.storage = storage;
    }

    private Storage storage;

    public StorageUnitBuilder classPathExplorer(ClassPathExplorer classPathExplorer) {
        this.classPathExplorer = classPathExplorer;
        return this;
    }

    private ClassPathExplorer classPathExplorer;

    public StorageUnit build() {
        StorageUnit unit = new StorageUnit();
        unit.storage = storage;
        unit.implementations = new ArrayList<>();

        List<EntityImplementation> aggregateRootImplementations = classPathExplorer.getDataAccessImplementations(storage).stream()
            .map(this::buildAggregateRootImplementation)
            .collect(toList());
        unit.implementations.addAll(aggregateRootImplementations);

        Set<Class<?>> aggregateRoots = unit.implementations.stream()
                .map(EntityImplementation::getEntityClass)
                .collect(toSet());

        @SuppressWarnings("rawtypes")
        Set<Class<EntityAttributes>> entityDataClasses = classPathExplorer.getDataImplementations(storage);
        for(@SuppressWarnings("rawtypes") Class<EntityAttributes> entityDataClass : entityDataClasses) {
            DataImplementation annotation = entityDataClass.getAnnotation(DataImplementation.class);
            if(aggregateRoots.contains(annotation.entity())) {
                throw new PousseCafeException("Aggregate root implementation can only be declared with @" + DataAccessImplementation.class.getSimpleName());
            }
        }

        List<EntityImplementation> nonRootEntityImplementations = entityDataClasses.stream()
                .map(this::buildNonRootEntityImplementation)
                .collect(toList());
        unit.implementations.addAll(nonRootEntityImplementations);

        return unit;
    }

    @SuppressWarnings("rawtypes")
    private EntityImplementation buildAggregateRootImplementation(Class<EntityDataAccess> entityDataAccessClass) {
        DataAccessImplementation annotation = entityDataAccessClass.getAnnotation(DataAccessImplementation.class);
        return new EntityImplementation.Builder()
                .withEntityClass(annotation.aggregateRoot())
                .withDataAccessFactory(() -> ReflectionUtils.newInstance(entityDataAccessClass))
                .withDataFactory(() -> ReflectionUtils.newInstance(annotation.dataImplementation()))
                .withStorage(storage)
                .build();
    }

    private EntityImplementation buildNonRootEntityImplementation(@SuppressWarnings("rawtypes") Class<EntityAttributes> entityDataClass) {
        DataImplementation annotation = entityDataClass.getAnnotation(DataImplementation.class);
        return new EntityImplementation.Builder()
                .withEntityClass(annotation.entity())
                .withDataFactory(() -> ReflectionUtils.newInstance(entityDataClass))
                .withStorage(storage)
                .build();
    }
}