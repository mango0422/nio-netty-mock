package com.example.nionetty.eventloop;

import com.example.nionetty.channel.ChannelFuture; // Needed for shutdown future
import com.example.nionetty.util.concurrent.Future; // Generic Future
import com.example.nionetty.util.concurrent.ScheduledExecutorService; // Extends Java's version

/**
 * A group of {@link EventLoop}s used to handle I/O and tasks for multiple {@link com.example.nionetty.channel.Channel}s.
 * Implementations typically manage a pool of threads.
 */
public interface EventLoopGroup extends ScheduledExecutorService { // Extends ScheduledExecutorService like Netty

    /**
     * Returns one of the {@link EventLoop}s that are managed by this {@link EventLoopGroup}.
     * The selection mechanism (e.g., round-robin) depends on the implementation.
     *
     * @return an {@link EventLoop}
     */
    EventLoop next();

    /**
     * Shut down this {@link EventLoopGroup} gracefully. Implementation should ensure all
     * associated {@link EventLoop}s are shut down.
     *
     * @return a future that will be notified when the shutdown process is complete.
     */
    Future<?> shutdownGracefully(); // Return our custom Future

    // Inherits methods from ScheduledExecutorService like submit, schedule, execute, shutdown, etc.
    // Implementations will need to provide these.
}