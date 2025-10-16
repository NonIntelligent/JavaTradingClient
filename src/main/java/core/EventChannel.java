package core;

import utility.Consumer;
import utility.TaskExecutor;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.concurrent.*;

public class EventChannel {
    private final BlockingQueue<AppEvent> events = new LinkedBlockingQueue<>();
    private final HashMap<Consumer, EnumSet<AppEventType>> consumers = new HashMap<>();
    private final TaskExecutor eventProcessor = new TaskExecutor(3);

    public void publish(AppEvent event) throws InterruptedException {
        events.put(event);
        eventProcessor.submitTask(this::notifySubscribers);
    }

    public void publish(AppEventType type, Consumer sender) throws InterruptedException {
        events.put(new AppEvent(null, type, sender));
        eventProcessor.submitTask(this::notifySubscribers);
    }

    public void publish(Object data, AppEventType type, Consumer sender) throws InterruptedException {
        events.put(new AppEvent(data, type, sender));
        eventProcessor.submitTask(this::notifySubscribers);
    }

    public void connectToService(Consumer consumer) {
        EnumSet<AppEventType> subscribed = EnumSet.noneOf(AppEventType.class);
        consumers.put(consumer, subscribed);
    }

    public void subscribeToEvent(Consumer consumer, AppEventType type) {
        EnumSet<AppEventType> acceptedEvents = consumers.get(consumer);
        if (acceptedEvents != null) {
            acceptedEvents.add(type);
        }
    }

    private void notifySubscribers() {
        AppEvent event = events.poll();
        if (event == null) return;

        for (var entry : consumers.entrySet()) {
            Consumer consumer = entry.getKey();
            EnumSet<AppEventType> acceptedEvents = entry.getValue();
            // Skip consumers that haven't subscribed to this event type
            if (!acceptedEvents.contains(event.type())) continue;
            // Skip sender of the event
            if (event.sender() == consumer) continue;

            consumer.processEvent(event);
        }
    }

    public void shutdown() {
        eventProcessor.shutdown();
    }
}
