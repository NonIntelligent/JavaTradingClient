package utility;

import java.util.Timer;
import java.util.TimerTask;

public class Timing {

    public Timer createTimer() {
        Timer timer = new Timer();
        return timer;
    }

    public void createTaskAtSchedule(TimerTask task, long delay) {
        Timer timer = new Timer();
        timer.schedule(task, delay);
        timer.scheduleAtFixedRate(task, delay, 1000L);
    }
}
