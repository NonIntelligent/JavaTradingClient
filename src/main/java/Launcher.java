import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Launcher {

    public static void main (String args[]){
        Logger log = LogManager.getLogger("surface");
        log.info("Hello World");

        UserInterface ui = new UserInterface();
        log.info("UI Launched");
        ui.Launch(args);


        log.info("End of app");
    }
}
