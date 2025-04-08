package com.example.nionetty.channel;

import java.net.SocketAddress;

/**
 * A nexus to a network socket or a component capable of I/O operations such as read, write, connect, and bind.
 * <p>
 * Represents a connection (e.g., TCP socket) and provides access to its configuration, pipeline,
 * event loop, and perform I/O operations asynchronously.
 */
public interface Channel extends Comparable<Channel> {

    /**
     * Returns the globally unique identifier of this Channel.
     */
    ChannelId id(); // Needs ChannelId class defined later

    /**
     * Return the EventLoop this Channel was registered to.
     */
    EventLoop eventLoop(); // Needs EventLoop interface defined

    /**
     * Returns the parent Channel which created this Channel.
     * Returns null for server channels or client channels not created via accept().
     */
    Channel parent();

    /**
     * Returns the configuration of this channel.
     */
    ChannelConfig config(); // Needs ChannelConfig interface defined

    /**
     * Returns true if the Channel is open and may be active.
     */
    boolean isOpen();

    /**
     * Returns true if the Channel is registered with an EventLoop.
     */
    boolean isRegistered();

    /**
     * Returns true if the Channel is active and connected (for sockets) or bound (for server sockets).
     */
    boolean isActive();

    /**
     * Return the ChannelPipeline for this channel, which handles inbound and outbound operations.
     */
    ChannelPipeline pipeline(); // Needs ChannelPipeline interface defined

    /**
     * Returns the local address where this channel is bound to.
     */
    SocketAddress localAddress();

    /**
     * Returns the remote address where this channel is connected to.
     */
    SocketAddress remoteAddress();

    /**
     * Request to bind the channel to the given SocketAddress and notify the ChannelFuture.
     */
    ChannelFuture bind(SocketAddress localAddress); // Needs ChannelFuture interface defined

    /**
     * Request to connect the channel to the given SocketAddress and notify the ChannelFuture.
     */
    ChannelFuture connect(SocketAddress remoteAddress);

    /**
     * Request to connect the channel to the given SocketAddress while binding to the localAddress and notify the ChannelFuture.
     */
    ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress);

    /**
     * Request to disconnect this Channel from its remote peer and notify the ChannelFuture.
     */
    ChannelFuture disconnect();

    /**
     * Request to close this Channel and notify the ChannelFuture.
     */
    ChannelFuture close();

    /**
     * Returns a ChannelFuture which will be notified when this channel is closed.
     */
    ChannelFuture closeFuture(); // Needs to be implemented, usually a promise fulfilled on close

    /**
     * Request to read new data from the Channel, potentially triggering channelRead events in the pipeline.
     * (Often implicitly handled by the event loop for sockets).
     */
    Channel read();

    /**
     * Request to write a message through the ChannelPipeline.
     * The actual write to the underlying transport will happen upon calling flush().
     */
    ChannelFuture write(Object msg);

    /**
     * Request to write a message through the ChannelPipeline and notify the ChannelPromise.
     */
    ChannelFuture write(Object msg, ChannelPromise promise); // Needs ChannelPromise interface defined

    /**
     * Request to flush all pending messages that were written but not yet flushed.
     */
    Channel flush();

    /**
     * Shortcut for calling write(msg) and flush().
     */
    ChannelFuture writeAndFlush(Object msg);

    /**
     * Shortcut for calling write(msg, promise) and flush().
     */
    ChannelFuture writeAndFlush(Object msg, ChannelPromise promise);

    /**
     * Return the unsafe operations object, allowing direct interaction with the transport,
     * typically only used by the EventLoop.
     */
    Unsafe unsafe(); // Needs Unsafe inner interface defined

    /**
     * Unsafe operations, intended for internal use by the transport implementation (e.g., EventLoop).
     * These methods often bypass the pipeline and interact directly with the underlying transport.
     */
    interface Unsafe {
        SocketAddress localAddress();
        SocketAddress remoteAddress();
        void register(EventLoop eventLoop, ChannelPromise promise);
        void bind(SocketAddress localAddress, ChannelPromise promise);
        void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise);
        void disconnect(ChannelPromise promise);
        void close(ChannelPromise promise);
        void closeForcibly();
        void beginRead();
        void write(Object msg, ChannelPromise promise);
        void flush();
    }
}

// // Placeholders required for Channel:
// package com.example.nionetty.channel;
// import java.util.concurrent.Future; // Base for ChannelFuture
// public interface ChannelId extends Comparable<ChannelId> { String asShortText(); String asLongText(); }
// public interface EventLoop { boolean inEventLoop(); void execute(Runnable task); /* ... */ }
// public interface ChannelConfig { /* ... */ }
// public interface ChannelPipeline { /* ... */ }
// public interface ChannelFuture extends Future<Void> { Channel channel(); boolean isSuccess(); Throwable cause(); ChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener); /* ... await, sync */ }
// public interface ChannelPromise extends ChannelFuture { ChannelPromise setSuccess(); ChannelPromise setFailure(Throwable cause); /* ... */ }
// import io.netty.util.concurrent.GenericFutureListener; // Or define a simple one