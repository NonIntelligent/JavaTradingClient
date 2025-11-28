package broker;

import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiFactoryTest {
    /**
     * Tests all Broker enums to see if they are constructable by the factory.
     * This does not require the extended API class to implement overrides.
     */
    @Test
    void are_all_apis_constructable() {
        assertDoesNotThrow(() ->{
            for (Broker broker: Broker.values()){
                ApiFactory.getApi(broker, AccountType.DEMO, "test", "test");
            }
        }, "Resolve by implementing the switch case in APIFactory." +
                " (Methods can still be unimplemented)");
    }

    /**
     * The test fails if the subclass was assigned the incorrect broker type
     * or if the wrong class was constructed by the factory.
     * !!!
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
    void doesFactoryThrowOnUnimplementedBroker() {
        final int MAX = Broker.values().length;

        // Generate array of all Broker values including a new one that isn't implemented.
        List<Broker> testEnums = new ArrayList<>();
        for (Broker b : Broker.values()) {
            testEnums.add(b);
        }

        Broker[] exceptionArray = new Broker[MAX + 1];

        // Replace "Broker.valueOf" method with an enum modification
        try (MockedStatic<Broker> brokerMock = Mockito.mockStatic(Broker.class)) {
            Broker unimplemented = Mockito.mock(Broker.class);
            Mockito.doReturn(MAX).when(unimplemented).ordinal();

            // Return the new values list to cause an exception.
            testEnums.add(unimplemented);
            testEnums.toArray(exceptionArray);
            brokerMock.when(Broker::values).thenReturn(exceptionArray);

            assertThrows(IllegalArgumentException.class, () ->
                    ApiFactory.getApi(unimplemented, AccountType.DEMO,
                            "test", "test"), "Exception thrown when attempting to create an API using an unimplemented Broker");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}