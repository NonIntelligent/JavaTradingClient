package broker;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class ApiFactoryTest {
    /**
     * Tests all Broker enums to see if they are constructable by the factory.
     * This does mean the class needs to be fully feature implemented.
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

    /**
     * TODO Requires a separate dependency to test the default case without changing written code
     */
    void doesFactoryThrowOnUnimplementedBroker() {
        // Replace "Broker.valueOf" method with an enum modification
        try {
            assertThrows(IllegalArgumentException.class, () ->
                    ApiFactory.getApi(Broker.valueOf("new"), AccountType.DEMO,
                            "test", "test"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}