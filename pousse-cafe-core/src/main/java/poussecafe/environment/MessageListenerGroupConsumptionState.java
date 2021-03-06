package poussecafe.environment;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import poussecafe.runtime.OriginalAndMarshaledMessage;

public class MessageListenerGroupConsumptionState {

    public static class Builder {

        private MessageListenerGroupConsumptionState  state = new MessageListenerGroupConsumptionState();

        public Builder message(OriginalAndMarshaledMessage message) {
            state.message = message;
            return this;
        }

        @SuppressWarnings("rawtypes")
        public Builder aggregateRootClass(Optional<Class> aggregateRootClass) {
            state.aggregateRootClass = aggregateRootClass;
            return this;
        }

        public Builder isFirstConsumption(boolean isFirstConsumption) {
            state.isFirstConsumption = isFirstConsumption;
            return this;
        }

        public Builder toUpdate(Set<Object> toUpdate) {
            state.toUpdate = toUpdate;
            return this;
        }

        public Builder processorLogger(Logger processorLogger) {
            state.processorLogger = processorLogger;
            return this;
        }

        public Builder hasUpdates(boolean hasUpdates) {
            state.hasUpdates = hasUpdates;
            return this;
        }

        public Builder consumptionId(String consumptionId) {
            state.consumptionId = consumptionId;
            return this;
        }

        public MessageListenerGroupConsumptionState build() {
            Objects.requireNonNull(state.message);
            Objects.requireNonNull(state.aggregateRootClass);
            Objects.requireNonNull(state.processorLogger);
            Objects.requireNonNull(state.consumptionId);
            return state;
        }
    }

    private MessageListenerGroupConsumptionState() {

    }

    private OriginalAndMarshaledMessage message;

    public OriginalAndMarshaledMessage message() {
        return message;
    }

    @SuppressWarnings("rawtypes")
    private Optional<Class> aggregateRootClass = Optional.empty();

    @SuppressWarnings("rawtypes")
    public Optional<Class> aggregateRootClass() {
        return aggregateRootClass;
    }

    public boolean isFirstConsumption() {
        return isFirstConsumption;
    }

    private boolean isFirstConsumption;

    public Set<Object> toUpdate() {
        return toUpdate;
    }

    private Set<Object> toUpdate = new HashSet<>();

    public Logger processorLogger() {
        return processorLogger;
    }

    private Logger processorLogger;

    private boolean hasUpdates;

    public boolean hasUpdates() {
        return hasUpdates;
    }

    private String consumptionId;

    public String consumptionId() {
        return consumptionId;
    }
}
