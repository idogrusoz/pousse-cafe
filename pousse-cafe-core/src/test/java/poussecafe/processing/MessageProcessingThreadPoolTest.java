package poussecafe.processing;

import org.junit.Test;
import poussecafe.apm.ApplicationPerformanceMonitoring;
import poussecafe.environment.MessageListenersPoolSplitStrategy;
import poussecafe.runtime.MessageConsumptionHandler;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageProcessingThreadPoolTest {

    @Test
    public void createdPoolHasExpectedNumberOfThreads() {
        givenExpectedNumberOfThreads();
        givenMessageListenersPoolSplitStrategy();
        givenListenersSet();
        givenMessageConsumptionHandler();
        givenApplicationPerformanceMonitoring();
        whenCreatingPool();
        thenCreatedPoolHasExpectedNumberOfThreads();
    }

    private void givenExpectedNumberOfThreads() {
        expectedNumberOfThreads = 8;
    }

    private int expectedNumberOfThreads;

    private void givenMessageListenersPoolSplitStrategy() {
        messageListenersPoolSplitStrategy = mock(MessageListenersPoolSplitStrategy.class);
    }

    private MessageListenersPoolSplitStrategy messageListenersPoolSplitStrategy;

    private void givenListenersSet() {
        listenersSet = mock(ListenersSet.class);
        ListenersSetPartition[] partitions = new ListenersSetPartition[expectedNumberOfThreads];
        for(int i = 0; i < partitions.length; ++i) {
            partitions[i] = mock(ListenersSetPartition.class);
        }
        when(listenersSet.split(messageListenersPoolSplitStrategy)).thenReturn(partitions);
    }

    private ListenersSet listenersSet;

    private void givenMessageConsumptionHandler() {
        messageConsumptionHandler = mock(MessageConsumptionHandler.class);
    }

    private MessageConsumptionHandler messageConsumptionHandler;

    private void givenApplicationPerformanceMonitoring() {
        applicationPerformanceMonitoring = mock(ApplicationPerformanceMonitoring.class);
    }

    private ApplicationPerformanceMonitoring applicationPerformanceMonitoring;

    private void whenCreatingPool() {
        pool = new MessageProcessingThreadPool.Builder()
                .messageListenersPoolSplitStrategy(messageListenersPoolSplitStrategy)
                .listenersSet(listenersSet)
                .messageConsumptionHandler(messageConsumptionHandler)
                .applicationPerformanceMonitoring(applicationPerformanceMonitoring)
                .messageConsumptionConfiguration(new MessageConsumptionConfiguration.Builder()
                        .build())
                .build();
    }

    private MessageProcessingThreadPool pool;

    private void thenCreatedPoolHasExpectedNumberOfThreads() {
        assertThat(pool.size(), is(expectedNumberOfThreads));
    }
}
