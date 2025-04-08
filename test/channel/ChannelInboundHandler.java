package com.example.nionetty.channel;

/**
 * {@link ChannelHandler} which handles inbound I/O events.
 * Events are typically triggered by I/O operations from the transport layer (e.g., data received, connection active).
 * Implementations should override the methods for the events they are interested in.
 * Events are propagated from the head to the tail of the pipeline by invoking the corresponding
 * `fire*` method on the {@link ChannelHandlerContext}.
 */
public interface ChannelInboundHandler extends ChannelHandler {

    /**
     * The {@link Channel} of the {@link ChannelHandlerContext} was registered with its {@link EventLoop}.
     * This method will be invoked entirely by the I/O thread.
     */
    void channelRegistered(ChannelHandlerContext ctx) throws Exception;

    /**
     * The {@link Channel} of the {@link ChannelHandlerContext} was unregistered from its {@link EventLoop}.
     * This method will be invoked entirely by the I/O thread.
     */
    void channelUnregistered(ChannelHandlerContext ctx) throws Exception;

    /**
     * The {@link Channel} of the {@link ChannelHandlerContext} is now active (e.g., connected or bound).
     * Data can usually be sent and received after this event.
     * This method will be invoked entirely by the I/O thread.
     */
    void channelActive(ChannelHandlerContext ctx) throws Exception;

    /**
     * The {@link Channel} of the {@link ChannelHandlerContext} was registered is now inactive and reached its
     * end of lifetime. No more I/O operations are possible.
     * This method will be invoked entirely by the I/O thread.
     */
    void channelInactive(ChannelHandlerContext ctx) throws Exception;

    /**
     * Invoked when the current {@link Channel} has read a message from the peer.
     * The message `msg` could be of any type, depending on the preceding handlers (e.g., decoders).
     * This method will be invoked entirely by the I/O thread.
     */
    void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception;

    /**
     * Invoked when the last message read by the current read operation has been consumed by
     * {@link #channelRead(ChannelHandlerContext, Object)}. If {@link ChannelConfig#isAutoRead()} is false,
     * no more messages will be read until {@link ChannelHandlerContext#read()} is called.
     * This method will be invoked entirely by the I/O thread.
     */
    void channelReadComplete(ChannelHandlerContext ctx) throws Exception;

    /**
     * Gets called when a user-defined event was triggered via {@link ChannelPipeline#fireUserEventTriggered(Object)}.
     * This method will be invoked entirely by the I/O thread.
     */
    void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception;

    /**
     * Gets called once the writable state of the {@link Channel} changed. You can check the state with
     * {@link Channel#isWritable()}.
     * This method will be invoked entirely by the I/O thread.
     */
    void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception;

    /**
     * Gets called if a {@link Throwable} was thrown by an upstream handler's event processing method
     * or by the transport layer during I/O. If not handled, the exception will reach the tail of the
     * pipeline and be logged.
     * This method will be invoked entirely by the I/O thread.
     *
     * @deprecated Prefer implementing {@link ChannelHandler#exceptionCaught(ChannelHandlerContext, Throwable)}
     * as this might be removed in future versions. This exists primarily for source compatibility.
     */
    @Override
    @Deprecated
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;
}
// // Placeholders required:
// package com.example.nionetty.channel;
// public interface ChannelHandler { /* handlerAdded, handlerRemoved, exceptionCaught */ }
// public interface ChannelHandlerContext { /* Methods to interact with pipeline, channel, executor */ }
// public interface Channel { boolean isWritable(); /* ... other methods */ }
// public interface ChannelConfig { boolean isAutoRead(); /* ... */ }
// public interface EventLoop { /* ... */ }
// public interface ChannelPipeline { ChannelPipeline fireUserEventTriggered(Object evt); /* ... */ }