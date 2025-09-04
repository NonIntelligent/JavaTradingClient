package core;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ui.FXLoading;
import utility.Consumer;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashMap;


public class EventsTest {

    public Field getPrivateField(Class<?> javaClass, String fieldName) throws NoSuchFieldException {
        Field field = javaClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    /**
     * Checks if every class that implement the Consumer interface
     * have been connected to the EventChannel after App creation.
     */
    @Test
    void are_all_consumers_connected() {
        try {
            Field channelField = getPrivateField(App.class, "eventChannel");
            Field consumerMap = getPrivateField(EventChannel.class, "consumers");
            App app = new App();
            EventChannel eventChannel = (EventChannel)channelField.get(app);
            var consumers = (HashMap<Consumer, EnumSet<AppEventType>>) consumerMap.get(eventChannel);

            assertEquals(2, consumers.size(),
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
            App app = new App();
            Manager manager = (Manager) managerField.get(app);
            FXLoading fxLoading = (FXLoading) fxLoadingField.get(app);

            manager.processEvent(new AppEvent(null, type));
            fxLoading.processEvent(new AppEvent(null, type));
            // Get all class implementations of Consumer and check if they are present in this list
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
