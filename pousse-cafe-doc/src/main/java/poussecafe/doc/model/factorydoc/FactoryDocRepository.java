package poussecafe.doc.model.factorydoc;

import java.util.List;
import poussecafe.doc.model.boundedcontextdoc.BoundedContextDocKey;
import poussecafe.domain.Repository;

public class FactoryDocRepository extends Repository<FactoryDoc, FactoryDocKey, FactoryDoc.Data> {

    public List<FactoryDoc> findByBoundedContextKey(BoundedContextDocKey key) {
        return newStorablesWithData(dataAccess().findByBoundedContextKey(key));
    }

    private FactoryDocDataAccess<FactoryDoc.Data> dataAccess() {
        return (FactoryDocDataAccess<FactoryDoc.Data>) dataAccess;
    }

    public List<FactoryDoc> findAll() {
        return newStorablesWithData(dataAccess().findAll());
    }

    public FactoryDoc findByBoundedContextKeyAndName(BoundedContextDocKey boundedContextDocKey,
            String factoryName) {
        return newStorableWithData(dataAccess().findByBoundedContextKeyAndName(boundedContextDocKey, factoryName));
    }
}