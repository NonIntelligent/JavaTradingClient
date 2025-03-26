package utility;

import java.util.concurrent.*;

public class TaskExecutor {
    private final ScheduledThreadPoolExecutor threadPool;

    public TaskExecutor(int minPoolSize) {
        threadPool = new ScheduledThreadPoolExecutor(minPoolSize);
        threadPool.setRemoveOnCancelPolicy(true);
    }

    public Future<?> submitTask(Runnable task) {
        return threadPool.submit(task);
    }

    public ScheduledFuture<?> scheduleTask(Runnable task, long startDelay) {
        return threadPool.schedule(task, startDelay, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> scheduleRepeatedTask(Runnable task, long startDelay, long period) {
        return threadPool.scheduleAtFixedRate(task, startDelay, period, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> scheduleDelayedTask(Runnable task, long startDelay, long delayBetweenTasks) {
        return threadPool.scheduleWithFixedDelay(task, startDelay, delayBetweenTasks, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        threadPool.purge();
        threadPool.shutdown();
        try {
            boolean terminated = threadPool.awaitTermination(1000L, TimeUnit.MILLISECONDS);
            if (!terminated) threadPool.shutdownNow();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdownNow() {
        threadPool.purge();
        threadPool.shutdownNow();
    }

}
