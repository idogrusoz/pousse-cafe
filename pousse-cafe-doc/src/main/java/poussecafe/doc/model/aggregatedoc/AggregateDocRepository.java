package poussecafe.doc.model.aggregatedoc;

import java.util.List;
import poussecafe.doc.model.moduledoc.ModuleDocId;
import poussecafe.domain.Repository;

public class AggregateDocRepository extends Repository<AggregateDoc, AggregateDocId, AggregateDoc.Attributes> {

    public List<AggregateDoc> findByModule(ModuleDocId id) {
        return wrap(dataAccess().findByModuleId(id));
    }

    @Override
    public AggregateDocDataAccess<AggregateDoc.Attributes> dataAccess() {
        return (AggregateDocDataAccess<AggregateDoc.Attributes>) super.dataAccess();
    }

    public List<AggregateDoc> findAll() {
        return wrap(dataAccess().findAll());
    }

    public List<AggregateDoc> findByIdClassName(String qualifiedName) {
        return wrap(dataAccess().findByIdClassName(qualifiedName));
    }
}
