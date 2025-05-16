package broker;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiFactoryTest {

    boolean factoryIsSound = false;

    /**
     * Tests all Broker enums to see if they are constructable by the factory.
     * This does mean the class needs to be fully feature implemented.
     */

    @Order(1)
    void are_all_apis_constructable() {
        assertDoesNotThrow(() ->{
            for (Broker broker: Broker.values()){
                ApiFactory.getApi(broker, AccountType.DEMO, "test", "test");
            }
        }, "Resolve by implementing the switch case");

        this.factoryIsSound = true;
    }

    /**
     * The test fails if the subclass was assigned the incorrect broker type
     * or if the wrong class was constructed by the factory.
     * !!!
     */

    @Order(2)
    void does_broker_type_match_api() {
        Assumptions.assumeTrue(factoryIsSound,
                "Factory cannot provide objects for all Brokers");

        for (Broker broker: Broker.values()){
            TradingAPI api = ApiFactory.getApi(broker, AccountType.DEMO, "test", "test");
            assertEquals(api.broker, broker);
        }
    }

    /**
     * TODO Requires dependency to test the default case without changing written code
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