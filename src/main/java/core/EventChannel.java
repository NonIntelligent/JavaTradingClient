package core;

import utility.Consumer;
import utility.TaskExecutor;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 * An event bus system that handles receiving and sending events to and from the {@code consumers}.
 * Events are handled using a thread pool and call the handler methods defined by the {@link Consumer}.
 */
public class EventChannel {
    private final BlockingQueue<AppEvent> events = new LinkedBlockingQueue<>();
    private final HashMap<Consumer, EnumSet<AppEventType>> consumers = new HashMap<>();
    private final TaskExecutor eventProcessor = new TaskExecutor(3);

    /**
     * Publish an event to be processed.
     * @param event The AppEvent object to be published.
     * @throws InterruptedException If you were interrupted whilst waiting to push an event.
     */
    public void publish(AppEvent event) throws InterruptedException {
        events.put(event);
        eventProcessor.submitTask(this::notifySubscribers);
    }

    /**
     * Publish an event with {@code null} data.
     * @param type The event type identifier.
     * @param sender The sender of this Event (typically {@code this}).
     * @throws InterruptedException If you were interrupted whilst waiting to push an event.
     */
    public void publish(AppEventType type, Consumer sender) throws InterruptedException {
        events.put(new AppEvent(null, type, sender));
        eventProcessor.submitTask(this::notifySubscribers);
    }

    /**
     * Publish and event with data.
     * @param data The data as a generic Object. The recipient is expected to know the type.
     * @param type The event type identifier.
     * @param sender the sender of This Event (typically {@code this}).
     * @throws InterruptedException If you were interrupted whilst waiting to push an event.
     */
    public void publish(Object data, AppEventType type, Consumer sender) throws InterruptedException {
        events.put(new AppEvent(data, type, sender));
        eventProcessor.submitTask(this::notifySubscribers);
    }

    /**
     * Connect to the event bus service.
     * @param consumer The consumer to link to.
     */
    public void connectToService(Consumer consumer) {
        EnumSet<AppEventType> subscribed = EnumSet.noneOf(AppEventType.class);
        consumers.put(consumer, subscribed);
    }

    /**
     * Consumers will subscribe to the specified events and only receive those.
     * Keeps a list of subscribed events for each consumer.
     * @param consumer The consumer that is subscribing.
     * @param type The event type to listen to.
     */
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

    /**
     * Shutdown.
     */
    public void shutdown() {
        eventProcessor.shutdown();
    }
}
