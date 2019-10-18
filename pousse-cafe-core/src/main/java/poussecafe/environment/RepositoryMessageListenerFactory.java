package poussecafe.environment;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import poussecafe.apm.ApmSpan;
import poussecafe.apm.ApplicationPerformanceMonitoring;
import poussecafe.domain.Repository;
import poussecafe.exception.PousseCafeException;
import poussecafe.exception.SameOperationException;
import poussecafe.messaging.Message;
import poussecafe.runtime.OptimisticLockingException;
import poussecafe.runtime.TransactionRunnerLocator;
import poussecafe.storage.TransactionRunner;
import poussecafe.util.MethodInvoker;

@SuppressWarnings({"rawtypes"})
public class RepositoryMessageListenerFactory {

    public static class Builder {

        private RepositoryMessageListenerFactory factory = new RepositoryMessageListenerFactory();

        public Builder environment(Environment environment) {
            factory.environment = environment;
            return this;
        }

        public Builder transactionRunnerLocator(TransactionRunnerLocator transactionRunnerLocator) {
            factory.transactionRunnerLocator = transactionRunnerLocator;
            return this;
        }

        public Builder applicationPerformanceMonitoring(ApplicationPerformanceMonitoring applicationPerformanceMonitoring) {
            factory.applicationPerformanceMonitoring = applicationPerformanceMonitoring;
            return this;
        }

        public RepositoryMessageListenerFactory build() {
            Objects.requireNonNull(factory.environment);
            Objects.requireNonNull(factory.transactionRunnerLocator);
            Objects.requireNonNull(factory.applicationPerformanceMonitoring);
            return factory;
        }
    }

    private RepositoryMessageListenerFactory() {

    }

    public MessageListener buildMessageListener(MessageListenerDefinition definition) {
        Method method = definition.method();
        Repository repository = environment.repository(definition.method().getDeclaringClass()).orElseThrow(PousseCafeException::new);
        Class<?> entityClass = repository.entityClass();
        MethodInvoker invoker = new MethodInvoker.Builder()
                .method(method)
                .target(repository)
                .rethrow(SameOperationException.class)
                .rethrow(OptimisticLockingException.class)
                .build();
        Optional<String> collisionSpace = definition.collisionSpace();
        if(collisionSpace.isEmpty()) {
            collisionSpace = Optional.of(repository.entityClass().getName());
        }
        return definition.messageListenerBuilder()
                .priority(MessageListenerPriority.REPOSITORY)
                .consumer(buildRepositoryMessageConsumer(entityClass, invoker))
                .collisionSpace(collisionSpace)
                .build();
    }

    private Environment environment;

    private Consumer<Message> buildRepositoryMessageConsumer(Class<?> entityClass, MethodInvoker invoker) {
        return message -> {
            TransactionRunner transactionRunner = transactionRunnerLocator.locateTransactionRunner(entityClass);
            transactionRunner.runInTransaction(() -> invokeInSpan(invoker, message));
        };
    }

    private TransactionRunnerLocator transactionRunnerLocator;

    private void invokeInSpan(MethodInvoker invoker, Message message) {
        ApmSpan span = applicationPerformanceMonitoring.currentSpan().startSpan();
        span.setName("repositoryInvocation");
        try {
            invoker.invoke(message);
        } catch (Exception e) {
            span.captureException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    private ApplicationPerformanceMonitoring applicationPerformanceMonitoring;
}
