package poussecafe.processing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import poussecafe.apm.ApplicationPerformanceMonitoring;
import poussecafe.environment.MessageListener;
import poussecafe.environment.MessageListenerConsumptionReport;
import poussecafe.environment.MessageListenerGroupConsumptionState;
import poussecafe.runtime.MessageConsumptionHandler;
import poussecafe.runtime.OriginalAndMarshaledMessage;
import poussecafe.util.ExponentialBackoff;

public class MessageConsumption {

    public static class Builder {

        private MessageConsumption consumption = new MessageConsumption();

        public Builder consumptionId(String consumptionId) {
            consumption.consumptionId = consumptionId;
            return this;
        }

        public Builder processorLogger(Logger processorLogger) {
            consumption.processorLogger = processorLogger;
            return this;
        }

        public Builder listenersPartition(ListenersSetPartition listenersPartition) {
            consumption.listenersPartition = listenersPartition;
            return this;
        }

        public Builder messageConsumptionHandler(MessageConsumptionHandler messageConsumptionHandler) {
            consumption.messageConsumptionHandler = messageConsumptionHandler;
            return this;
        }

        public Builder applicationPerformanceMonitoring(ApplicationPerformanceMonitoring applicationPerformanceMonitoring) {
            consumption.applicationPerformanceMonitoring = applicationPerformanceMonitoring;
            return this;
        }

        public Builder message(OriginalAndMarshaledMessage message) {
            consumption.message = message;
            return this;
        }

        public Builder messageConsumptionConfiguration(MessageConsumptionConfiguration messageConsumptionConfiguration) {
            consumption.messageConsumptionConfiguration = messageConsumptionConfiguration;
            return this;
        }

        public MessageConsumption build() {
            Objects.requireNonNull(consumption.consumptionId);
            Objects.requireNonNull(consumption.listenersPartition);
            Objects.requireNonNull(consumption.messageConsumptionHandler);
            Objects.requireNonNull(consumption.applicationPerformanceMonitoring);
            Objects.requireNonNull(consumption.processorLogger);
            Objects.requireNonNull(consumption.message);
            Objects.requireNonNull(consumption.messageConsumptionConfiguration);

            consumption.messageConsumptionState = new MessageConsumptionState.Builder()
                    .consumptionId(consumption.consumptionId)
                    .message(consumption.message)
                    .processorLogger(consumption.processorLogger)
                    .build();

            return consumption;
        }
    }

    private MessageConsumption() {

    }

    private String consumptionId;

    private OriginalAndMarshaledMessage message;

    public void execute() {
        processorLogger.debug("Handling received message {}", message.original());
        List<MessageListenerGroup> groups = buildMessageListenerGroups();
        logGroups(groups);
        if(!groups.isEmpty()) {
            List<MessageListenerGroup> toRetryInitially = consumeMessageOrRetryGroups(groups);
            if(!toRetryInitially.isEmpty()) {
                retryConsumption(toRetryInitially);
            }
        }
        processorLogger.debug("Message {} handled (consumption ID {})", message.original(), consumptionId);
    }

    private void logGroups(List<MessageListenerGroup> groups) {
        if(processorLogger.isDebugEnabled()) {
            processorLogger.debug("Built {} groups:", groups.size());
            for(MessageListenerGroup group : groups) {
                processorLogger.debug("    group {}", group.aggregateRootClass());
                for(MessageListener listener : group.listeners()) {
                    processorLogger.debug("        - {}", listener.shortId());
                }
            }
        }
    }

    private List<MessageListenerGroup> buildMessageListenerGroups() {
        Collection<MessageListener> listeners = listenersPartition.partitionListenersSet()
                .messageListenersOf(message.original().getClass());
        return new MessageListenersGroupsFactory.Builder()
                .applicationPerformanceMonitoring(applicationPerformanceMonitoring)
                .message(message)
                .messageConsumptionHandler(messageConsumptionHandler)
                .logger(processorLogger)
                .build()
                .buildMessageListenerGroups(listeners);
    }

    private ListenersSetPartition listenersPartition;

    private List<MessageListenerGroup> consumeMessageOrRetryGroups(List<MessageListenerGroup> groups) {
        List<MessageListenerGroup> toRetry = new ArrayList<>();
        for (MessageListenerGroup group : groups) {
            List<MessageListenerConsumptionReport> reports = consumeMessage(group);
            boolean mustRetry = reports.stream().anyMatch(MessageListenerConsumptionReport::mustRetry);
            if(mustRetry) {
                toRetry.add(group);
            }
        }
        return toRetry;
    }

    private List<MessageListenerConsumptionReport> consumeMessage(MessageListenerGroup group) {
        MessageListenerGroupConsumptionState consumptionState = messageConsumptionState
                .buildMessageListenerGroupState(group);
        List<MessageListenerConsumptionReport> reports = group.consumeMessageOrRetry(consumptionState);
        messageConsumptionState.update(group, reports);
        return reports;
    }

    private void retryConsumption(List<MessageListenerGroup> toRetryInitially) {
        messageConsumptionState.isFirstConsumption(false);
        ExponentialBackoff exponentialBackoff = new ExponentialBackoff.Builder()
                .slotTime(messageConsumptionConfiguration.backOffSlotTime())
                .ceiling(messageConsumptionConfiguration.backOffCeiling())
                .build();
        int retry = 1;
        List<MessageListenerGroup> toRetry = toRetryInitially;
        long cumulatedWaitTime = 0;
        while(!toRetry.isEmpty() && retry <= messageConsumptionConfiguration.maxConsumptionRetries()) {
            long waitTime = (long) Math.ceil(exponentialBackoff.nextValue());
            processorLogger.warn("Retrying consumption of {} for {} groups in {} ms", message.original().getClass().getSimpleName(), toRetry.size(), waitTime);
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                processorLogger.error("Thread was interrupted during backoff");
                Thread.currentThread().interrupt();
                break;
            }
            toRetry = consumeMessageOrRetryGroups(toRetry);
            cumulatedWaitTime += waitTime;
            ++retry;
        }
        if(!toRetry.isEmpty()) {
            processorLogger.error("Reached max. # of retries ({}), giving up handling of {} with {} remaining groups", messageConsumptionConfiguration.maxConsumptionRetries(), message.original().getClass().getName(), toRetry.size());
            processorLogger.error("Unhandled message: {}", message.marshaled());
        } else {
            processorLogger.info("Message {} successfully consumed after {} retries ({} ms cumulated wait time)", message.original().getClass().getSimpleName(), (retry - 1), cumulatedWaitTime);
        }
    }

    private MessageConsumptionConfiguration messageConsumptionConfiguration;

    protected Logger processorLogger;

    private MessageConsumptionState messageConsumptionState;

    private MessageConsumptionHandler messageConsumptionHandler;

    private ApplicationPerformanceMonitoring applicationPerformanceMonitoring;
}
