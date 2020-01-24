package poussecafe.discovery;

import poussecafe.domain.EntityAttributes;
import poussecafe.domain.Factory;
import poussecafe.runtime.TestDomainEvent;
import poussecafe.runtime.TestDomainEvent2;
import poussecafe.runtime.TestDomainEvent3;

public class FactoryWithSingleListener extends Factory<String, AggregateRootWithSingleListener, EntityAttributes<String>> {

    @MessageListener
    @ProducesEvent(event = TestDomainEvent2.class, required = false)
    @ProducesEvent(event = TestDomainEvent3.class)
    public AggregateRootWithSingleListener listener(TestDomainEvent event) {
        return new AggregateRootWithSingleListener();
    }
}
