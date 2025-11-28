package broker;

import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiFactoryTest {

    private static MockedStatic<Broker> brokerMock;
    private static Broker unimplemented;

    @BeforeAll
    public static void setUp() {
        final int MAX = Broker.values().length;

        unimplemented = Mockito.mock(Broker.class);
        Mockito.doReturn(MAX).when(unimplemented).ordinal();

        // Generate array of all Broker values including a new one that isn't implemented.
        EnumSet<Broker> enums = EnumSet.allOf(Broker.class);
        Broker[] exceptionArray = new Broker[MAX + 1];
        enums.toArray(exceptionArray);

        exceptionArray[MAX] = unimplemented;

        // Return modified array on any iteration for Broker.values()
        brokerMock = Mockito.mockStatic(Broker.class);
        brokerMock.when(Broker::values).thenReturn(exceptionArray);
    }

    @AfterAll
    public static void tearDown() {
        brokerMock.close();
    }

    /**
     * Tests all Broker enums to see if they are constructable by the factory.
     * This does not require the extended API class to implement overrides.
     */
    @Test
    void are_all_apis_constructable() {
        // Only iterate the explicitly defined Enums.
        Broker[] definedBrokers = Broker.values();
        int definedLength = definedBrokers.length - 1;
        assertDoesNotThrow(() -> {
            for (int i = 0; i < definedLength; i++){
                Broker broker = definedBrokers[i];
                ApiFactory.getApi(broker, AccountType.DEMO, "test", "test");
            }
        }, "Resolve by implementing the switch case in APIFactory." +
                " (Methods can still be unimplemented)");
    }

    /**
     * The test only fails if the subclass was assigned the incorrect broker type
     * or if the wrong class was constructed by the factory.
     * !!! This ignores Brokers that are not handled by the ApiFactory !!!
     */
    @Test
    void does_broker_type_match_api() {
        for (Broker broker: Broker.values()) {
            TradingAPI api;
            try {
                api = ApiFactory.getApi(broker, AccountType.DEMO, "test", "test");
            } catch (IllegalArgumentException e) {
                continue;
            }
            assertEquals(api.broker, broker);
        }
    }

    @Test
    void does_factory_throw_on_unimplementedBroker() {
        try {
            assertThrows(IllegalArgumentException.class, () ->
                    ApiFactory.getApi(unimplemented, AccountType.DEMO,
                            "test", "test"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}