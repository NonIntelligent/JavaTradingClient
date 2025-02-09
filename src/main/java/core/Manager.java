package core;

public class Manager {
    private ApiHandler apiHandler;

    Manager(){
        apiHandler = new ApiHandler();
    }

    public ApiHandler getBrokerHandler() {
        return apiHandler;
    }
}
