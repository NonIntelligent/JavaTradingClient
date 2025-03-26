package core;

import utility.Consumer;
import utility.TaskExecutor;

import java.util.List;
import java.util.concurrent.*;

public class EventChannel {
    private final BlockingQueue<AppEvent> events = new LinkedBlockingQueue<>();
    private final List<Consumer> consumers = new CopyOnWriteArrayList<>();
    private final TaskExecutor eventProcessor = new TaskExecutor(3);

    public void publish(AppEvent event) throws InterruptedException {
        events.put(event);
        eventProcessor.submitTask(this::notifySubscribers);
    }

    public void publish(Object data, AppEventType type) throws InterruptedException {
        AppEvent event = new AppEvent(data, type);
        events.put(event);
        eventProcessor.submitTask(this::notifySubscribers);
    }

    public void subscribe(Consumer consumer) {
        consumers.add(consumer);
    }

    private void notifySubscribers() {
        AppEvent event = events.poll();
        if (event == null) return;

        for (Consumer consumer : consumers) {
            consumer.processEvent(event);
        }
    }

    public void shutdown() {
        eventProcessor.shutdown();
    }
}
