package poussecafe.domain;

import poussecafe.context.Aggregate;

@Aggregate(
  factory = SimpleAggregateFactory.class,
  repository = SimpleAggregateRepository.class
)
public class SimpleAggregate extends AggregateRoot<SimpleAggregateKey, SimpleAggregate.Data> {

    public static interface Data extends EntityData<SimpleAggregateKey> {

    }

}
