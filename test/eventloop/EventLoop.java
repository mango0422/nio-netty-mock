package com.example.nionetty.eventloop;

import com.example.nionetty.channel.Channel;
import com.example.nionetty.channel.ChannelFuture;
import com.example.nionetty.channel.ChannelPromise; // For register return type maybe? Netty uses ChannelFuture.

/**
 * An {@link EventLoop} handles all the I/O operations for a {@link Channel} once registered.
 * A single {@link EventLoop} instance is usually run by a single thread and handles multiple channels.
 * It also executes tasks submitted to it.
 */
public interface EventLoop extends EventLoopGroup { // An EventLoop is an EventLoopGroup of size 1

    /**
     * Returns a reference to the {@link EventLoopGroup} which this {@link EventLoop} belongs to.
     */
    @Override
    EventLoopGroup parent(); // Provide a more specific return type than ExecutorService's parent method (if any)

    /**
     * Returns {@code true} if the current thread is the thread managed by this {@link EventLoop}.
     *
     * @return {@code true} if the current thread is the event loop thread, {@code false} otherwise.
     */
    boolean inEventLoop();

    /**
     * Creates a new {@link com.example.nionetty.channel.ChannelPromise} associated with this event loop.
     *
     * @return a new {@link ChannelPromise}
     */
    ChannelPromise newPromise();

    /**
     * Registers a {@link Channel} with this {@link EventLoop}.
     * Once registered, all I/O operations and event handling for the channel will be processed by this loop.
     * The registration itself might happen asynchronously.
     *
     * @param channel the {@link Channel} to register
     * @return the {@link ChannelFuture} which will be notified once the registration finishes
     */
    ChannelFuture register(Channel channel);

    // Inherits execute, submit, schedule methods from EventLoopGroup/ScheduledExecutorService
}