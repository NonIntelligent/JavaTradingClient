package core;

import utility.EventConsumer;

import java.util.function.Consumer;

/**
 * Data structure package to help send event data for consumers.
 * @param data The data to send.
 * @param type The type of event correlated to the data being sent.
 * @param sender The sender of this event.
 */
public record AppEvent(Object data, AppEventType type, EventConsumer sender) {
    public static final Consumer<Object> NOOP = any -> {};
}