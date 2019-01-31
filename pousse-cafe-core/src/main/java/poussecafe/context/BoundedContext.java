package poussecafe.context;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import poussecafe.domain.EntityImplementation;
import poussecafe.messaging.MessageImplementation;

public class BoundedContext {

    public static class Builder {

        private BoundedContext boundedContext = new BoundedContext();

        public Builder definition(BoundedContextDefinition definition) {
            boundedContext.definition = definition;
            return this;
        }

        public Builder storageImplementations(Collection<EntityImplementation> storageImplementations) {
            boundedContext.storageImplementations.addAll(storageImplementations);
            return this;
        }

        public Builder messagingImplementations(Collection<MessageImplementation> messagingImplementations) {
            boundedContext.messagingImplementations.addAll(messagingImplementations);
            return this;
        }

        public Builder serviceImplementations(Collection<ServiceImplementation> serviceImplementations) {
            boundedContext.serviceImplementations.addAll(serviceImplementations);
            return this;
        }

        public BoundedContext build() {
            Objects.requireNonNull(boundedContext.definition);
            Objects.requireNonNull(boundedContext.storageImplementations);
            Objects.requireNonNull(boundedContext.messagingImplementations);
            Objects.requireNonNull(boundedContext.serviceImplementations);
            return boundedContext;
        }
    }

    private BoundedContext() {

    }

    private BoundedContextDefinition definition;

    public BoundedContextDefinition definition() {
        return definition;
    }

    private Set<EntityImplementation> storageImplementations = new HashSet<>();

    public Set<EntityImplementation> storageImplementations() {
        return storageImplementations;
    }

    private Set<MessageImplementation> messagingImplementations = new HashSet<>();

    public Set<MessageImplementation> messagingImplementations() {
        return messagingImplementations;
    }

    private Set<ServiceImplementation> serviceImplementations = new HashSet<>();

    public Set<ServiceImplementation> serviceImplementations() {
        return serviceImplementations;
    }
}
