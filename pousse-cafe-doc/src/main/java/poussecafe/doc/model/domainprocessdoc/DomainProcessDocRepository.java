package poussecafe.doc.model.domainprocessdoc;

import java.util.List;
import poussecafe.doc.model.moduledoc.ModuleDocId;
import poussecafe.domain.Repository;

public class DomainProcessDocRepository extends Repository<DomainProcessDoc, DomainProcessDocId, DomainProcessDoc.Attributes> {

    public List<DomainProcessDoc> findAll() {
        return wrap(dataAccess().findAll());
    }

    @Override
    public DomainProcessDocDataAccess<DomainProcessDoc.Attributes> dataAccess() {
        return (DomainProcessDocDataAccess<DomainProcessDoc.Attributes>) super.dataAccess();
    }

    public List<DomainProcessDoc> findByModuleId(ModuleDocId id) {
        return wrap(dataAccess().findByModuleId(id));
    }
}
