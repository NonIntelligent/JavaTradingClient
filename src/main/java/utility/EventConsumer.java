package utility;

import core.AppEvent;

/**
 * Apply to classes that act as a consumer for {@link AppEvent} objects in an event bus system.
 */
public interface EventConsumer {
    void processEvent(AppEvent event);
    void startUpSubscribedEvents();
}
