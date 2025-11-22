package core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.mockito.Mockito.*;

import org.mockito.Mockito;
import ui.FXLoading;
import utility.EventConsumer;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashMap;


public class EventsTest {
    private static App sharedApp;
    private static Manager mockManager;
    private static FXLoading mockEventSender;

    public Field getPrivateField(Class<?> javaClass, String fieldName) throws NoSuchFieldException {
        Field field = javaClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    @BeforeAll
    static void AppSetup() {
        sharedApp = new App();
        mockManager = mock(Manager.class);
        mockEventSender = mock(FXLoading.class);

        doNothing().when(mockManager).processEvent(any());
    }

    @Test
    void check_subscribers_are_notified() {
        EventChannel eventChannel = new EventChannel();
        eventChannel.connectToService(mockManager);
        eventChannel.subscribeToEvent(mockManager, AppEventType.DEMO_APP);
        eventChannel.subscribeToEvent(mockManager, AppEventType.CREATE_ACCOUNT);

        try {
            eventChannel.publish(AppEventType.DEMO_APP, mockEventSender);
            eventChannel.publish(new Object(), AppEventType.DEMO_APP, mockEventSender);
            eventChannel.publish(null, AppEventType.CREATE_ACCOUNT, mockEventSender);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        eventChannel.shutdown();
        verify(mockManager, times(3)).processEvent(any());
    }

    @Test
    void is_sender_of_event_skipped() {
        EventChannel eventChannel = new EventChannel();
        eventChannel.connectToService(mockManager);
        eventChannel.subscribeToEvent(mockManager, AppEventType.DEMO_APP);

        try {
            eventChannel.publish(AppEventType.DEMO_APP, mockManager);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        eventChannel.shutdown();
        verify(mockManager, times(0)).processEvent(any());
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

    @Test
    void do_consumers_handle_events_safely() {
        try {
            Field managerField = getPrivateField(App.class, "manager");
            Field fxLoadingField = getPrivateField(App.class, "fxLoading");
            Manager manager = (Manager) managerField.get(sharedApp);
            FXLoading fxLoading = (FXLoading) fxLoadingField.get(sharedApp);

            manager.processEvent(new AppEvent(null, AppEventType.DEMO_APP, manager));
            fxLoading.processEvent(new AppEvent(null, AppEventType.CREATE_ACCOUNT, fxLoading));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
