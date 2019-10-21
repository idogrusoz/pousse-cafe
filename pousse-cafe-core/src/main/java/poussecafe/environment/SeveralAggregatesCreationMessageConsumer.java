package poussecafe.environment;

import java.util.Objects;
import java.util.function.Consumer;
import poussecafe.apm.ApmSpan;
import poussecafe.apm.ApplicationPerformanceMonitoring;
import poussecafe.domain.AggregateRoot;
import poussecafe.domain.Repository;
import poussecafe.messaging.Message;
import poussecafe.runtime.TransactionRunnerLocator;
import poussecafe.storage.TransactionRunner;
import poussecafe.util.MethodInvoker;

@SuppressWarnings({"unchecked", "rawtypes"})
public class SeveralAggregatesCreationMessageConsumer implements Consumer<Message> {

    public static class Builder {

        private SeveralAggregatesCreationMessageConsumer consumer = new SeveralAggregatesCreationMessageConsumer();

        public Builder invoker(MethodInvoker invoker) {
            consumer.invoker = invoker;
            return this;
        }

        public Builder transactionRunnerLocator(TransactionRunnerLocator transactionRunnerLocator) {
            consumer.transactionRunnerLocator = transactionRunnerLocator;
            return this;
        }

        public Builder aggregateServices(AggregateServices aggregateServices) {
            consumer.aggregateServices = aggregateServices;
            return this;
        }

        public Builder applicationPerformanceMonitoring(ApplicationPerformanceMonitoring applicationPerformanceMonitoring) {
            consumer.applicationPerformanceMonitoring = applicationPerformanceMonitoring;
            return this;
        }

        public SeveralAggregatesCreationMessageConsumer build() {
            Objects.requireNonNull(consumer.transactionRunnerLocator);
            Objects.requireNonNull(consumer.aggregateServices);
            Objects.requireNonNull(consumer.invoker);
            return consumer;
        }
    }

    private SeveralAggregatesCreationMessageConsumer() {

    }

    @Override
    public void accept(Message message) {
        Iterable<AggregateRoot> iterable = createAggregates(message);
        Class entityClass = aggregateServices.aggregateRootEntityClass();
        TransactionRunner transactionRunner = transactionRunnerLocator.locateTransactionRunner(entityClass);
        Repository repository = aggregateServices.repository();
        for(AggregateRoot aggregate : iterable) {
            addCreatedAggregate(transactionRunner, repository, aggregate);
        }
    }

    private Iterable<AggregateRoot> createAggregates(Message message) {
        ApmSpan span = applicationPerformanceMonitoring.currentSpan().startSpan();
        span.setName(invoker.method().getName());
        try {
            return (Iterable<AggregateRoot>) invoker.invoke(message);
        } finally {
            span.end();
        }
    }

    private MethodInvoker invoker;

    private ApplicationPerformanceMonitoring applicationPerformanceMonitoring;

    private TransactionRunnerLocator transactionRunnerLocator;

    private AggregateServices aggregateServices;

    private void addCreatedAggregate(TransactionRunner transactionRunner,
            Repository repository,
            AggregateRoot aggregate) {
        transactionRunner.runInTransaction(() -> repository.add(aggregate));
    }
}
