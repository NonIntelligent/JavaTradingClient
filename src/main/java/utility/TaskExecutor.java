package utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;

/**
 * Schedule tasks to run using a scheduled thread pool.
 * The result of the tasks is managed by the caller through the {@link Future} object.
 */
public class TaskExecutor {
    private static final Logger log = LoggerFactory.getLogger(TaskExecutor.class);
    private final ScheduledThreadPoolExecutor threadPool;

    /**
     * Initialises a thread pool of a minimum size
     * as well as enables tasks to be removed immediately from the queue after being cancelled.
     * @param minPoolSize The number of threads to keep even when idle, {@code > 0}.
     */
    public TaskExecutor(int minPoolSize) {
        threadPool = new ScheduledThreadPoolExecutor(minPoolSize);
        threadPool.setRemoveOnCancelPolicy(true);
    }

    /**
     * Submits a one-time task to be executed.
     * @param task The method to be called.
     * @return A Future representing the completion state of the task.
     * The Future's {@code get} method returns {@code null} on completion.
     */
    public Future<?> submitTask(Runnable task) {
        return threadPool.submit(task);
    }

    /**
     * Schedule a task to be run a single-time after a given delay in milliseconds.
     * @param task The method to be called.
     * @param startDelay The time before beginning execution in milliseconds.
     * @return A ScheduledFuture representing the completion of the task.
     * The object's {@code get} method returns {@code null} on completion.
     */
    public ScheduledFuture<?> scheduleTask(Runnable task, long startDelay) {
        return threadPool.schedule(task, startDelay, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedule a fixed-rate periodic task after a given start-up delay in milliseconds.
     * Will run until the {@code Future} explicitly cancelled or the service is {@link #shutdown}.
     * @param task The method to be called.
     * @param startDelay The time before beginning execution in milliseconds.
     * @param period The amount of time that passes from the start of the task before repeating.
     *               Includes the execution time of the task as well.
     * @return A ScheduledFuture representing the completion of the task.
     * The object's {@code get} method returns {@code null} on completion.
     */
    public ScheduledFuture<?> scheduleRepeatedTask(Runnable task, long startDelay, long period) {
        return threadPool.scheduleAtFixedRate(task, startDelay, period, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedule a periodic task after a given start-up delay in milliseconds.
     * Will run until the {@code Future} explicitly cancelled or the service is {@link #shutdown}.
     * @param task The method to be called.
     * @param startDelay The time before beginning execution in milliseconds.
     * @param delayBetweenTasks The amount of time that must pass between the completion of the task
     *                         and the beginning of the next in milliseconds.
     * @return A ScheduledFuture representing the completion of the task.
     * The object's {@code get} method returns {@code null} on completion.
     */
    public ScheduledFuture<?> scheduleRepeatedDelayTask(Runnable task, long startDelay, long delayBetweenTasks) {
        return threadPool.scheduleWithFixedDelay(task, startDelay, delayBetweenTasks, TimeUnit.MILLISECONDS);
    }

    /**
     * Debugging method to check if the task returns without an exception.
     * @param future The {@code Future} object returned from a task submission.
     * @return If it was possible to call the Future's {@code get} method without an exception.
     */
    public boolean isSuccessful(Future<?> future) {
        boolean success = false;
        try {
            future.get();
            success = true;
        } catch (InterruptedException e) {
            log.debug("The current thread was interrupted whist waiting.", e);
        } catch (ExecutionException e) {
            log.error("The task itself had an issue during execution", e);
        } catch (CancellationException e) {
            log.debug("Task was cancelled", e);
        }

        return success;
    }

    /**
     * Cancel a given task (typically a repeated one).
     * @param future The {@code Future} object returned from a task submission.
     * @param mayInterrupt If you want to cancel a task that is mid-execution.
     * @return If the task has been cancelled before completion.
     */
    public boolean cancelTask(Future<?> future, boolean mayInterrupt) {
        future.cancel(mayInterrupt);
        return future.isCancelled();
    }

    /**
     * Clear all pending tasks from the task queue of the thread pool.
     */
    public void cancelAllTasks() {
        threadPool.getQueue().clear();
    }

    /**
     * Gracefully shutdown the {@code ThreadPoolExecutor} and awaits the termination
     * of all currently executing tasks for 1 second.
     */
    public void shutdown() {
        log.info("Terminating and shutting down executor pool");
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(1000L, TimeUnit.MILLISECONDS);
            threadPool.purge();
        } catch (InterruptedException e) {
            log.error("Current thread {} was interrupted whilst waiting for termination", Thread.currentThread(), e);
        }
    }

    /**
     * Forcefully shutdown the {@code ThreadPoolExecutor} by interrupting all currently running tasks.
     */
    public void shutdownNow() {
        threadPool.shutdownNow();
        threadPool.purge();
    }

}
