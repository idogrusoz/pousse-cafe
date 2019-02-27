package poussecafe.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import poussecafe.domain.AggregateRoot;
import poussecafe.domain.DomainEvent;
import poussecafe.domain.EntityAttributes;
import poussecafe.domain.Repository;
import poussecafe.environment.EntityImplementation;
import poussecafe.exception.PousseCafeException;
import poussecafe.messaging.MessageReceiver;
import poussecafe.messaging.MessagingConnection;
import poussecafe.messaging.internal.InternalMessagingQueue.InternalMessageReceiver;
import poussecafe.runtime.AggregateServices;
import poussecafe.runtime.Runtime;
import poussecafe.storage.internal.InternalDataAccess;
import poussecafe.storage.internal.InternalStorage;

import static poussecafe.check.AssertionSpecification.value;
import static poussecafe.check.Checks.checkThat;

public class RuntimeWrapper {

    public RuntimeWrapper(Runtime context) {
        Objects.requireNonNull(context);
        this.context = context;
    }

    private Runtime context;

    private JsonDataReader jsonDataReader = new JsonDataReader();

    public Runtime runtime() {
        return context;
    }

    @SuppressWarnings("unchecked")
    public <T extends AggregateRoot<K, D>, K, D extends EntityAttributes<K>> T find(Class<T> entityClass,
            K key) {
        waitUntilAllMessageQueuesEmpty();
        Repository<AggregateRoot<K, D>, K, D> repository = (Repository<AggregateRoot<K, D>, K, D>) context
                .environment()
                .repositoryOf(entityClass)
                .orElseThrow(PousseCafeException::new);
        return (T) repository.find(key);
    }

    public void waitUntilAllMessageQueuesEmpty() {
        try {
            for(MessagingConnection connection : context.messagingConnections()) {
                MessageReceiver receiver = connection.messageReceiver();
                if(receiver instanceof InternalMessageReceiver) {
                    InternalMessageReceiver internalMessageReceiver = (InternalMessageReceiver) receiver;
                    internalMessageReceiver.queue().waitUntilEmpty();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PousseCafeException(e);
        }
    }

    public void addDomainEvent(DomainEvent event) {
        context.messageSenderLocator().locate(event.getClass()).sendMessage(event);
        waitUntilAllMessageQueuesEmpty();
    }

    public void loadDataFile(String path) {
        try {
            String json = new String(Files.readAllBytes(Paths.get(getClass().getResource(path).toURI())),
                    StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.reader().readTree(json);
            jsonNode.fieldNames().forEachRemaining(entityClassName -> {
                loadEntity(entityClassName, jsonNode);
            });
        } catch (Exception e) {
            throw new PousseCafeException("Unable to load data file", e);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void loadEntity(String entityClassName, JsonNode jsonNode) {
        logger.info("Loading data for entity {}", entityClassName);
        try {
            Class<?> entityClass = Class.forName(entityClassName);
            EntityImplementation entityImplementation = context.environment().entityImplementation(entityClass);
            checkThat(value(entityImplementation.getStorage() == InternalStorage.instance()).isTrue());

            AggregateServices services = context.environment().aggregateServicesOf(entityClass).orElseThrow(PousseCafeException::new);
            InternalDataAccess dataAccess = (InternalDataAccess) services.getRepository().dataAccess();
            logger.debug("Field value {}", jsonNode.get(entityClassName));
            jsonNode.get(entityClassName).elements().forEachRemaining(dataJson -> {
                logger.debug("Loading {}", dataJson);
                EntityAttributes<?> dataImplementation = (EntityAttributes<?>) entityImplementation.getDataFactory().get();
                jsonDataReader.readJson(dataImplementation, dataJson);
                dataAccess.addData(dataImplementation);
            });
        } catch (ClassNotFoundException e) {
            throw new PousseCafeException("No entity with class " + entityClassName, e);
        }
    }

    private Logger logger = LoggerFactory.getLogger(getClass());
}