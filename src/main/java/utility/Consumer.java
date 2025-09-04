package utility;

import core.AppEvent;

public interface Consumer {
    void processEvent(AppEvent event);
    void startUpSubscribedEvents();
}
