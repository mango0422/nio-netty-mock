package com.example.nionetty.eventloop;

import com.example.nionetty.channel.Channel;
import com.example.nionetty.channel.ChannelFuture;
import com.example.nionetty.channel.ChannelPromise;
import com.example.nionetty.channel.DefaultChannelPromise; // Need an implementation
import com.example.nionetty.util.concurrent.AbstractScheduledEventExecutor; // Need this base class
import com.example.nionetty.util.concurrent.Future;      // Need our Future
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract base class for single-threaded {@link EventLoop} implementations.
 * Manages the thread execution, task queue, and shutdown logic.
 * Subclasses must implement the {@link #run()} method containing the core I/O processing loop (e.g., selector logic).
 */
public abstract class SingleThreadEventLoop extends AbstractScheduledEventExecutor implements EventLoop {

    private static final Logger log = LoggerFactory.getLogger(SingleThreadEventLoop.class);
    private static final int ST_NOT_STARTED = 1;
    private static final int ST_STARTED = 2;
    private static final int ST_SHUTTING_DOWN = 3;
    private static final int ST_SHUTDOWN = 4;
    private static final int ST_TERMINATED = 5;

    private final EventLoopGroup parent;
    private final Queue<Runnable> taskQueue;
    private final Executor executor; // Usually a ThreadPerTaskExecutor
    private final AtomicReference<Thread> thread = new AtomicReference<>();
    private volatile int state = ST_NOT_STARTED;
    private final CountDownLatch terminatedLatch = new CountDownLatch(1); // To await termination


    protected SingleThreadEventLoop(EventLoopGroup parent, Executor executor) {
        this.parent = parent;
        this.executor = executor;
        this.taskQueue = newTaskQueue(); // Allow subclasses to provide queue type
    }

    /**
     * Returns the queue used for storing tasks. Subclasses can override to provide custom queue implementations.
     *
     * @return A new ConcurrentLinkedQueue by default.
     */
    protected Queue<Runnable> newTaskQueue() {
        return new ConcurrentLinkedQueue<>();
    }

    @Override
    public EventLoopGroup parent() {
        return parent;
    }

    @Override
    public EventLoop next() {
        // An EventLoop is an EventLoopGroup of size 1, so it always returns itself.
        return this;
    }

    @Override
    public boolean inEventLoop() {
        return Thread.currentThread() == thread.get();
    }

    /**
     * Adds a task to the task queue and wakes up the event loop thread if necessary.
     *
     * @param task the task to execute
     */
    @Override
    public void execute(Runnable task) {
        if (task == null) {
            throw new NullPointerException("task");
        }
        addTask(task);
        startThread(); // Ensure thread is started
    }

    protected void addTask(Runnable task) {
        if (!offerTask(task)) {
            reject(task); // Task queue full or rejected policy
        }
    }

    protected final boolean offerTask(Runnable task) {
        if (isShuttingDown()) {
            reject();
        }
        return taskQueue.offer(task);
    }

    protected boolean hasTasks() {
        return !taskQueue.isEmpty();
    }

    /**
     * Run all tasks currently pending in the queue.
     *
     * @return the number of tasks run, or -1 if the limit was reached.
     */
    protected long runAllTasks() {
        long runTasks = 0;
        Runnable task;
        while ((task = pollTask()) != null) {
            try {
                task.run();
            } catch (Throwable t) {
                log.warn("A task raised an exception.", t);
            }
            runTasks++;
        }
        return runTasks;
    }

    protected Runnable pollTask() {
        return taskQueue.poll();
    }

    protected Runnable peekTask() {
        return taskQueue.peek();
    }


    private void startThread() {
        if (state == ST_NOT_STARTED) {
            if (STATE_UPDATER.compareAndSet(this, ST_NOT_STARTED, ST_STARTED)) {
                log.debug("Starting event loop thread");
                boolean success = false;
                try {
                    doStartThread();
                    success = true;
                } finally {
                    if (!success) {
                        STATE_UPDATER.compareAndSet(this, ST_STARTED, ST_NOT_STARTED); // Rollback state on failure
                    }
                }
            }
        }
    }

    private void doStartThread() {
        executor.execute(() -> {
            thread.set(Thread.currentThread());
            boolean success = false;
            try {
                // Abstract method to be implemented by subclasses (e.g., NioEventLoop)
                // This method should contain the core processing loop (select, process keys, run tasks)
                SingleThreadEventLoop.this.run();
                success = true;
            } catch (Throwable t) {
                log.error("Unexpected exception in event loop thread", t);
            } finally {
                // Loop cleanup (state transitions, confirmation)
                cleanupAndTerminate(success);
            }
        });
    }


    /**
     * Abstract method representing the core execution loop for this EventLoop.
     * Implementations should handle I/O events (if applicable) and execute tasks from the queue.
     * This method should run until the event loop is shut down.
     */
    protected abstract void run();


    @Override
    public Future<?> shutdownGracefully() {
        // Simplified shutdown logic
        if (STATE_UPDATER.compareAndSet(this, ST_STARTED, ST_SHUTTING_DOWN)) {
            log.info("EventLoop shutdown initiated.");
            // Wake up loop if necessary (subclass responsibility, e.g., selector.wakeup())
            wakeup(inEventLoop()); // Add a wakeup method if needed
        } else if (state >= ST_SHUTTING_DOWN) {
            log.debug("EventLoop already shutting down or shut down.");
        } else {
             // Not started yet, transition directly to terminated maybe?
            STATE_UPDATER.set(this, ST_SHUTDOWN); // Or TERMINATED?
            terminatedLatch.countDown();
            log.debug("EventLoop shutdown immediately as it was not started.");
        }
        // Return a future that completes on termination (needs implementation)
        // For now, return a placeholder. Real impl needs a promise fulfilled in cleanupAndTerminate.
        return new DefaultChannelPromise(null,this).setSuccess(null); // HACK: Use ChannelPromise as Future
    }

    @Override
    public boolean isShuttingDown() {
         return state >= ST_SHUTTING_DOWN;
    }

    @Override
    public boolean isShutdown() {
        return state >= ST_SHUTDOWN;
    }

    @Override
    public boolean isTerminated() {
        return state == ST_TERMINATED;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return terminatedLatch.await(timeout, unit);
    }

    /**
     * Wake up the event loop thread if it's blocked (e.g., in selector.select()).
     * Should be implemented by subclasses if blocking occurs.
     *
     * @param inEventLoop {@code true} if called from within the event loop thread.
     */
    protected void wakeup(boolean inEventLoop) {
        // Default implementation does nothing. Subclasses (like NioEventLoop) override.
    }


    /**
     * Cleanup resources and confirm termination.
     * @param success Indicates if the run() method completed successfully or via exception.
     */
    protected void cleanupAndTerminate(boolean success) {
         // Transition state to shutdown/terminated
         for (;;) {
             int oldState = state;
             if (oldState >= ST_SHUTTING_DOWN || STATE_UPDATER.compareAndSet(this, oldState, ST_SHUTTING_DOWN)) {
                 break;
             }
         }
         // Best effort cleanup of tasks etc.
         confirmShutdown();
    }

     protected void confirmShutdown() {
        if (!isShuttingDown()) {
            return; // Should not happen, but guard
        }
         if (state == ST_TERMINATED) {
             return; // Already terminated
         }

         // Run final tasks? Cancel scheduled tasks? (Simplified here)
         runAllTasks(); // Run any final tasks added during shutdown

         // Transition to TERMINATED
         STATE_UPDATER.set(this, ST_TERMINATED);
         terminatedLatch.countDown(); // Signal termination completion
         log.info("EventLoop terminated.");
    }

    protected void reject() {
        throw new RejectedExecutionException("EventLoop is shutting down");
    }

    protected static void reject(Runnable task) {
         throw new RejectedExecutionException("Task " + task + " rejected");
    }


    // --- Implementation of EventLoop specific methods ---
    @Override
    public ChannelPromise newPromise() {
        return new DefaultChannelPromise(null, this); // Channel might be null initially
    }

    // Abstract method from parent, needs concrete implementation in subclasses like NioEventLoop
    @Override
    public abstract ChannelFuture register(Channel channel);

    // Need to implement other ScheduledExecutorService methods (schedule, submit etc.)
    // This requires more complex handling of scheduled tasks, deadlines, etc.
    // For simplicity in this mock, we might leave them unimplemented or provide basic versions.
    // Example using AbstractScheduledEventExecutor methods (if that base class handles scheduling):
    // @Override public ScheduledFuture<?> schedule(...) { return super.schedule(...); }

    // Need AtomicIntegerFieldUpdater for state management (like Netty)
    private static final AtomicIntegerFieldUpdater<SingleThreadEventLoop> STATE_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(SingleThreadEventLoop.class, "state");

}

// Need a base AbstractScheduledEventExecutor that handles scheduling logic (complex)
// package com.example.nionetty.util.concurrent;
// public abstract class AbstractScheduledEventExecutor extends AbstractExecutorService implements ScheduledExecutorService { /* complex scheduling logic */ }
// public abstract class AbstractExecutorService implements ExecutorService { /* basic impl */ }