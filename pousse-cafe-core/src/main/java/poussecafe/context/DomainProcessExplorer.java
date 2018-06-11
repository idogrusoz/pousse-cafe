package poussecafe.context;

import poussecafe.messaging.MessageListenerRegistry;

import static poussecafe.check.AssertionSpecification.value;
import static poussecafe.check.Checks.checkThat;

public class DomainProcessExplorer {

    private MessageListenerRegistry registry;

    public void setMessageListenerRegistry(MessageListenerRegistry registry) {
        checkThat(value(registry).notNull().because("Message listener registry cannot be null"));
        this.registry = registry;
    }

    public void discoverListeners(Object service) {
        checkThat(value(service).notNull().because("Service cannot be null"));
        DomainEventListenerExplorer explorer = new DomainEventListenerExplorer(registry, service);
        explorer.discoverListeners();
    }

}
