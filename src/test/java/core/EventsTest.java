package core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ui.FXLoading;
import utility.EventConsumer;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashMap;


public class EventsTest {
    private static App sharedApp;

    public Field getPrivateField(Class<?> javaClass, String fieldName) throws NoSuchFieldException {
        Field field = javaClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    @BeforeAll
    static void AppSetup() {
        sharedApp = new App();
    }

    /**
     * Checks if every class that implement the Consumer interface
     * have been connected to the EventChannel after App creation.
     */
    private static final int NO_OF_CONSUMERS = 2;
    @Test
    void are_all_consumers_connected() {
        try {
            Field channelField = getPrivateField(App.class, "eventChannel");
            Field consumerMap = getPrivateField(EventChannel.class, "consumers");
            EventChannel eventChannel = (EventChannel)channelField.get(sharedApp);
            var consumers = (HashMap<EventConsumer, EnumSet<AppEventType>>) consumerMap.get(eventChannel);

            assertEquals(NO_OF_CONSUMERS, consumers.size(),
                    "All in-use consumers have been connected to the event channel");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests if there are any issues with handling events that provide null data.
     * Do they fail safely and continue process or throw an exception?
     * @param type An event type that consumers may subscribe to and handle
     */
    @ParameterizedTest
    @EnumSource(AppEventType.class)
    void do_consumers_handle_events_safely(AppEventType type) {
        try {
            Field managerField = getPrivateField(App.class, "manager");
            Field fxLoadingField = getPrivateField(App.class, "fxLoading");
            Manager manager = (Manager) managerField.get(sharedApp);
            FXLoading fxLoading = (FXLoading) fxLoadingField.get(sharedApp);

            manager.processEvent(new AppEvent(null, type, manager));
            fxLoading.processEvent(new AppEvent(null, type, fxLoading));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
