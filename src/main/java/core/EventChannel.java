package core;

import utility.EventConsumer;
import utility.TaskExecutor;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 * An event bus system that handles receiving and sending events to and from the {@code consumers}.
 * Events are handled using a thread pool and call the handler methods defined by the {@link EventConsumer}.
 */
public class EventChannel {
    private final BlockingQueue<AppEvent> events = new LinkedBlockingQueue<>();
    private final HashMap<EventConsumer, EnumSet<AppEventType>> consumers = new HashMap<>();
    private final TaskExecutor eventProcessor = new TaskExecutor(3);

    /**
     * Publish an event with {@code NO-OP} data.
     * @param type The event type identifier.
     * @param sender The sender of this Event (typically {@code this}).
     * @throws InterruptedException If you were interrupted whilst waiting to push an event.
     */
    public void publish(AppEventType type, EventConsumer sender) throws InterruptedException {
        events.put(new AppEvent(AppEvent.NOOP, type, sender));
        eventProcessor.submitTask(this::notifySubscribers);
    }

    /**
     * Publish and event with data.
     * @param data The data as a generic Object. The recipient is expected to know the type.
     * @param type The event type identifier.
     * @param sender the sender of This Event (typically {@code this}).
     * @throws InterruptedException If you were interrupted whilst waiting to push an event.
     */
    public void publish(Object data, AppEventType type, EventConsumer sender) throws InterruptedException {
        events.put(new AppEvent(data, type, sender));
        eventProcessor.submitTask(this::notifySubscribers);
    }

    /**
     * Connect to the event bus service.
     * @param eventConsumer The consumer to link to.
     */
    public void connectToService(EventConsumer eventConsumer) {
        EnumSet<AppEventType> subscribed = EnumSet.noneOf(AppEventType.class);
        consumers.put(eventConsumer, subscribed);
    }

    /**
     * Consumers will subscribe to the specified events and only receive those.
     * Keeps a list of subscribed events for each consumer.
     * @param eventConsumer The consumer that is subscribing.
     * @param type The event type to listen to.
     */
    public void subscribeToEvent(EventConsumer eventConsumer, AppEventType type) {
        EnumSet<AppEventType> acceptedEvents = consumers.get(eventConsumer);
        if (acceptedEvents != null) {
            acceptedEvents.add(type);
        }
    }

    private void notifySubscribers() {
        AppEvent event = events.poll();
        if (event == null) return;

        for (var entry : consumers.entrySet()) {
            EventConsumer eventConsumer = entry.getKey();
            EnumSet<AppEventType> acceptedEvents = entry.getValue();
            // Skip consumers that haven't subscribed to this event type
            if (!acceptedEvents.contains(event.type())) continue;
            // Skip sender of the event
            if (event.sender() == eventConsumer) continue;

            eventConsumer.processEvent(event);
        }
    }

    /**
     * Checks if the data is intentionally specified as NO-OP.
     * @param data Event data.
     * @return If the object is a NO-OP.
     */
    public boolean isNoop(Object data) {
        return AppEvent.NOOP == data;
    }

    /**
     * Shutdown.
     */
    public void shutdown() {
        eventProcessor.shutdown();
    }
}
