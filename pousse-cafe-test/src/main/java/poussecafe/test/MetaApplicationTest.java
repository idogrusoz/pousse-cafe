package poussecafe.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import poussecafe.context.MetaApplicationBundle;
import poussecafe.context.MetaApplicationContext;
import poussecafe.context.StorableServices;
import poussecafe.domain.DomainEvent;
import poussecafe.exception.PousseCafeException;
import poussecafe.storable.IdentifiedStorable;
import poussecafe.storable.IdentifiedStorableData;
import poussecafe.storable.IdentifiedStorableRepository;
import poussecafe.storable.StorableImplementation;
import poussecafe.storage.memory.InMemoryDataAccess;
import poussecafe.storage.memory.InMemoryStorage;

import static poussecafe.check.AssertionSpecification.value;
import static poussecafe.check.Checks.checkThat;

public abstract class MetaApplicationTest {

    private MetaApplicationContext context;

    private JsonDataReader jsonDataReader = new JsonDataReader();

    @Before
    public void configureContext() {
        context = new MetaApplicationContext();
        for(MetaApplicationBundle bundle : testBundle()) {
            context.loadBundle(bundle);
        }
        context.start();
        context.injectDependencies(this);
        context.registerListeners(this);
    }

    protected abstract List<MetaApplicationBundle> testBundle();

    protected MetaApplicationContext context() {
        return context;
    }

    @SuppressWarnings("unchecked")
    protected <T extends IdentifiedStorable<K, D>, K, D extends IdentifiedStorableData<K>> T find(Class<T> storableClass,
            K key) {
        waitUntilAllMessageQueuesEmpty();
        IdentifiedStorableRepository<IdentifiedStorable<K, D>, K, D> repository = (IdentifiedStorableRepository<IdentifiedStorable<K, D>, K, D>) context
                .getStorableServices(storableClass)
                .getRepository();
        return (T) repository.find(key);
    }

    protected void waitUntilAllMessageQueuesEmpty() {
        try {
            context.getInMemoryQueue().waitUntilEmpty();
        } catch (InterruptedException e) {
            return;
        }
    }

    protected void addDomainEvent(DomainEvent event) {
        context.getMessageSender().sendMessage(event);
        waitUntilAllMessageQueuesEmpty();
    }

    protected void loadDataFile(String path) {
        try {
            String json = new String(Files.readAllBytes(Paths.get(getClass().getResource(path).toURI())),
                    Charset.forName("UTF-8"));

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.reader().readTree(json);
            jsonNode.fieldNames().forEachRemaining(storableClassName -> {
                loadStorable(storableClassName, jsonNode);
            });
        } catch (Exception e) {
            throw new PousseCafeException("Unable to load data file", e);
        }
    }

    private void loadStorable(String storableClassName, JsonNode jsonNode) {
        logger.info("Loading data for storable " + storableClassName);
        try {
            Class<?> storableClass = Class.forName(storableClassName);
            StorableImplementation storableImplementation = context.environment().getStorableImplementation(storableClass);
            checkThat(value(storableImplementation.getStorage() == InMemoryStorage.instance()).isTrue());

            StorableServices services = context.getStorableServices(storableClass);
            InMemoryDataAccess dataAccess = (InMemoryDataAccess) services.getRepository().getDataAccess();
            logger.debug("Field value " + jsonNode.get(storableClassName));
            jsonNode.get(storableClassName).elements().forEachRemaining(dataJson -> {
                logger.debug("Loading " + dataJson.toString());
                IdentifiedStorableData<?> dataImplementation = (IdentifiedStorableData<?>) storableImplementation.getDataFactory().get();
                jsonDataReader.readJson(dataImplementation, dataJson);
                dataAccess.addData(dataImplementation);
            });
        } catch (ClassNotFoundException e) {
            throw new PousseCafeException("No storable with class " + storableClassName, e);
        }
    }

    private Logger logger = LoggerFactory.getLogger(getClass());
}
