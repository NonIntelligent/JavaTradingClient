package utility;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskExecutor {
    private final ScheduledThreadPoolExecutor threadPool;

    public TaskExecutor() {
        threadPool = new ScheduledThreadPoolExecutor(4);
    }

    public Future submitTask(Runnable task) {
        return threadPool.submit(task);
    }

    public Future scheduleTask(Runnable task, long startDelay) {
        return threadPool.schedule(task, startDelay, TimeUnit.MILLISECONDS);
    }

    public Future scheduleRepeatedTask(Runnable task, long startDelay, long period) {
        return threadPool.scheduleAtFixedRate(task, startDelay, period, TimeUnit.MILLISECONDS);
    }

    public Future scheduleDelayedTask(Runnable task, long startDelay, long delayBetweenTasks) {
        return threadPool.scheduleWithFixedDelay(task, startDelay, delayBetweenTasks, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        threadPool.purge();
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(1000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
