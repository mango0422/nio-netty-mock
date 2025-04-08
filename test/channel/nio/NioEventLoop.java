package com.example.nionetty.channel.nio;

import com.example.nionetty.channel.Channel; // Assuming exists
import com.example.nionetty.channel.ChannelFuture; // Assuming exists
import com.example.nionetty.channel.DefaultChannelPromise; // Assuming exists
import com.example.nionetty.channel.EventLoop; // Assuming exists
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * NIO implementation of an EventLoop that handles I/O using a Selector.
 */
public class NioEventLoop implements EventLoop, Runnable {

    private static final Logger log = LoggerFactory.getLogger(NioEventLoop.class);

    private final NioEventLoopGroup parent;
    private final Executor executor; // Executes this event loop task (the run() method)
    private final Selector selector;
    private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();
    private volatile Thread thread; // The thread executing this event loop
    private final AtomicBoolean started = new AtomicBoolean(false);
    private volatile boolean shuttingDown = false;

    public NioEventLoop(NioEventLoopGroup parent, Executor executor) throws IOException {
        this.parent = parent;
        this.executor = executor;
        this.selector = Selector.open();
        start(); // Start the event loop thread upon creation
    }

    private void start() {
        if (started.compareAndSet(false, true)) {
            executor.execute(this);
            log.debug("NioEventLoop started.");
        }
    }

    @Override
    public void run() {
        this.thread = Thread.currentThread();
        log.info("{} started running.", Thread.currentThread().getName());

        while (!shuttingDown) {
            try {
                // Select operation with a timeout (e.g., 1 second)
                // If timeout is 0, it blocks indefinitely until event or wakeup()
                // Using a timeout allows checking the task queue periodically even without I/O events
                int selectedKeys = selector.select(1000); // Timeout in milliseconds

                // Run tasks submitted from other threads or during I/O handling
                runAllTasks(); // Process tasks added via execute()

                if (selectedKeys > 0) {
                     // Process I/O events
                    processSelectedKeys(selector.selectedKeys());
                }

                // Check shutdown status again after processing keys and tasks
                if (shuttingDown) {
                     break;
                }

            } catch (ClosedSelectorException e) {
                log.warn("Selector closed, exiting loop.", e);
                break; // Exit loop if selector is closed
            }
            catch (IOException e) {
                // Handle selector exceptions (e.g., spurious wakeups, interrupted select)
                log.warn("Selector exception in {}: {}", Thread.currentThread().getName(), e.getMessage(), e);
                // Rebuild selector? Or just log and continue? Netty might rebuild.
                rebuildSelector();
            } catch (Exception e) {
                 log.error("Unexpected error in event loop {}", Thread.currentThread().getName(), e);
            }
        }

        // Cleanup before exiting
        closeSelector();
        log.info("{} finished running.", Thread.currentThread().getName());
    }

    private void processSelectedKeys(Set<SelectionKey> selectedKeys) {
        if (selectedKeys.isEmpty()) {
            return;
        }
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            keyIterator.remove(); // Remove the key from the set!

            // Retrieve the channel associated with the key (attached earlier)
            // We need a way to link SelectionKey back to our Channel abstraction
            final Object attachment = key.attachment();

             if (!(attachment instanceof AbstractNioChannel)) {
                 log.warn("SelectionKey attachment is not an AbstractNioChannel: {}", attachment);
                 // Potentially cancel key if attachment is wrong?
                 key.cancel();
                 continue;
             }

            final AbstractNioChannel channel = (AbstractNioChannel) attachment;
            // Ensure the key is still valid
            if (!key.isValid()) {
                log.debug("Key is invalid, skipping: {}", key);
                channel.close(); // Ensure channel resources are cleaned up if key is invalid
                continue;
            }

            try {
                int readyOps = key.readyOps();
                log.trace("Processing key: {}, readyOps: {}", key, readyOps);

                // Check for OP_ACCEPT
                if ((readyOps & SelectionKey.OP_ACCEPT) != 0) {
                    // Server channel accepting a new connection
                    // The channel itself should handle the accept logic
                    channel.handleNioRead(); // NioServerSocketChannel's read method handles accept
                }

                 // Check for OP_CONNECT (for client channels)
                if ((readyOps & SelectionKey.OP_CONNECT) != 0) {
                    channel.handleNioConnect();
                }

                // Check for OP_READ before OP_WRITE, as reading might trigger writes
                 if ((readyOps & SelectionKey.OP_READ) != 0) {
                    // Channel ready for reading data
                     channel.handleNioRead();
                }

                // Check for OP_WRITE
                if ((readyOps & SelectionKey.OP_WRITE) != 0) {
                     // Channel ready for writing data
                    channel.handleNioWrite();
                }

            } catch (CancelledKeyException e) {
                 log.debug("Selection key cancelled for channel {}", channel, e);
                 channel.close(); // Ensure cleanup if key was cancelled externally
            }
            catch (Exception e) {
                log.error("Error handling key {} for channel {}", key, channel, e);
                // Close the channel on error to prevent infinite loops or leaks
                channel.close();
            }
        }
    }


    /**
     * Executes a task in the event loop thread. If the current thread *is* the event loop thread,
     * executes immediately. Otherwise, adds to the queue and wakes up the selector.
     */
    @Override
    public void execute(Runnable task) {
        if (inEventLoop()) {
            log.trace("Executing task immediately in event loop");
            task.run();
        } else {
             log.trace("Adding task to queue and waking up selector");
            taskQueue.offer(task);
            // Wake up the selector if it's blocked in select()
            // This is crucial for responsiveness when tasks are added externally
            selector.wakeup();
        }
    }

    private void runAllTasks() {
         Runnable task;
        while ((task = taskQueue.poll()) != null) {
             try {
                 log.trace("Running task from queue: {}", task.getClass().getSimpleName());
                task.run();
             } catch (Exception e) {
                log.error("Task execution failed", e);
            }
        }
    }

    @Override
    public boolean inEventLoop() {
        return Thread.currentThread() == this.thread;
    }

    /**
     * Registers a channel with this event loop's selector.
     * Must be called from within the event loop thread.
     * @param channel The channel wrapper
     * @return A future that completes when registration is done.
     */
    @Override
    public ChannelFuture register(Channel channel) {
        // This registration needs to happen *in* the event loop thread.
        // We return a promise and schedule the actual registration.
        DefaultChannelPromise promise = new DefaultChannelPromise(channel, this); // Needs Promise impl

        execute(() -> {
            try {
                if (!(channel instanceof AbstractNioChannel)) {
                     throw new IllegalArgumentException("Channel is not an AbstractNioChannel: " + channel.getClass());
                }
                AbstractNioChannel nioChannel = (AbstractNioChannel) channel;
                // Register the underlying NIO channel with the selector
                // Attach our Channel wrapper object to the key for later retrieval
                SelectionKey selectionKey = nioChannel.javaChannel().register(selector, 0, nioChannel); // Initially interestOps = 0
                nioChannel.setSelectionKey(selectionKey); // Store the key in our channel wrapper
                nioChannel.setEventLoop(this); // Assign this loop to the channel
                log.info("Registered channel {} with selector", channel);
                promise.setSuccess(); // Notify successful registration

                // Trigger pipeline activation (e.g., fireChannelRegistered, fireChannelActive if connected/bound)
                 // channel.pipeline().fireChannelRegistered(); // Needs pipeline impl
                 // if (channel.isActive()) { // Needs isActive impl
                 //    channel.pipeline().fireChannelActive();
                 // }


            } catch (Exception e) {
                log.error("Failed to register channel {}", channel, e);
                 // Need to clean up if registration fails
                promise.setFailure(e);
                channel.close(); // Close the channel if registration fails
            }
        });

        return promise;
    }


    /**
     * Deregisters a channel (cancels its SelectionKey). Must be called from EventLoop thread.
     */
    public void deregister(AbstractNioChannel channel) {
         if (!inEventLoop()) {
             execute(() -> deregister(channel));
             return;
         }
         SelectionKey key = channel.selectionKey();
         if (key != null && key.isValid()) {
             log.debug("Cancelling selection key for channel {}", channel);
             key.cancel();
         }
         // Further cleanup might be needed
    }


    public void shutdown() {
         log.info("Shutdown requested for {}", Thread.currentThread().getName());
        shuttingDown = true;
        // Wake up the selector if it's blocked, so the loop can check the flag
        selector.wakeup();
    }

    private void closeSelector() {
        try {
             log.debug("Closing selector for {}", Thread.currentThread().getName());
            selector.close();
        } catch (IOException e) {
            log.warn("Failed to close selector", e);
        }
    }

     private void rebuildSelector() {
         // In a real implementation, might try to close old selector, open a new one,
         // and re-register all valid keys. This is complex.
         log.warn("Selector rebuild requested but not implemented. Closing the loop.");
         // Simple approach: just shut down on selector error.
         closeSelector();
         shutdown();
    }


    // // Placeholder Interfaces needed:
    // package com.example.nionetty.channel;
    // import java.net.SocketAddress;
    // import java.nio.channels.SelectionKey;
    // public interface EventLoop extends Executor {
    //     boolean inEventLoop();
    //     void execute(Runnable command);
    //     ChannelFuture register(Channel channel);
    //     // EventLoopGroup parent(); ?
    // }
    // public interface ChannelPromise extends ChannelFuture { ChannelPromise setSuccess(); ChannelPromise setFailure(Throwable cause); /* ... */ }
    // public class DefaultChannelPromise implements ChannelPromise { /* constructor, methods... */ }
    // public abstract class AbstractNioChannel implements Channel {
    //     abstract java.nio.channels.SelectableChannel javaChannel();
    //     abstract SelectionKey selectionKey();
    //     abstract void setSelectionKey(SelectionKey key);
    //     abstract void setEventLoop(NioEventLoop loop);
    //     abstract void handleNioRead();
    //     abstract void handleNioWrite();
    //     abstract void handleNioConnect();
    //     abstract void close(); // Abstract close or common implementation
    // }
}