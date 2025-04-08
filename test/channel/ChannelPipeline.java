package com.example.nionetty.channel;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

/**
 * A list of {@link ChannelHandler}s which handles or intercepts inbound events and outbound operations
 * of a {@link Channel}. {@link ChannelPipeline} implements an advanced form of the Intercepting Filter pattern
 * to give a user full control over how an event is handled and how the handlers interact with each other.
 * <p>
 * Handlers are organized as a doubly-linked list. Inbound events flow from head to tail,
 * while outbound operations flow from tail to head.
 */
public interface ChannelPipeline extends Iterable<Map.Entry<String, ChannelHandler>> {

    /**
     * Inserts a {@link ChannelHandler} at the first position of this pipeline.
     *
     * @param name    the name of the handler to insert first. Use {@code null} to let the pipeline generate a unique name.
     * @param handler the handler to insert first
     * @return {@code this} pipeline
     */
    ChannelPipeline addFirst(String name, ChannelHandler handler);

    /**
     * Appends a {@link ChannelHandler} at the last position of this pipeline.
     *
     * @param name    the name of the handler to append. Use {@code null} to let the pipeline generate a unique name.
     * @param handler the handler to append
     * @return {@code this} pipeline
     */
    ChannelPipeline addLast(String name, ChannelHandler handler);

    // Other modification methods (addBefore, addAfter) could be added here.

    /**
     * Removes the specified {@link ChannelHandler} from this pipeline.
     *
     * @param handler the handler to remove
     * @return {@code this} pipeline
     */
    ChannelPipeline remove(ChannelHandler handler);

    /**
     * Removes the {@link ChannelHandler} with the specified name from this pipeline.
     *
     * @param name the name under which the handler was added
     * @return the removed handler
     */
    ChannelHandler remove(String name);

    /**
     * Removes the first {@link ChannelHandler} in this pipeline.
     *
     * @return the removed handler
     */
    ChannelHandler removeFirst();

    /**
     * Removes the last {@link ChannelHandler} in this pipeline.
     *
     * @return the removed handler
     */
    ChannelHandler removeLast();

    /**
     * Returns the {@link ChannelHandler} with the specified name in this pipeline.
     *
     * @param name the name of the handler
     * @return the handler with the specified name, or {@code null} if there's no such handler
     */
    ChannelHandler get(String name);

    /**
     * Returns the {@link ChannelHandler} of the specified type in this pipeline.
     *
     * @param handlerType the type of the handler
     * @return the handler of the specified handler type, or {@code null} if there's no such handler
     */
    <T extends ChannelHandler> T get(Class<T> handlerType);

    /**
     * Returns the context object of the {@link ChannelHandler} with the specified name in this pipeline.
     *
     * @param name the name of the handler
     * @return the context object for the specified handler, or {@code null} if there's no such handler
     */
    ChannelHandlerContext context(String name);

    /**
     * Returns the context object of the specified {@link ChannelHandler} in this pipeline.
     *
     * @param handler the handler instance
     * @return the context object for the specified handler, or {@code null} if the handler is not in the pipeline
     */
    ChannelHandlerContext context(ChannelHandler handler);

    /**
     * Returns the context object of the {@link ChannelHandler} of the specified type in this pipeline.
     *
     * @param handlerType the type of the handler
     * @return the context object for the handler of the specified type, or {@code null} if there's no such handler
     */
    ChannelHandlerContext context(Class<? extends ChannelHandler> handlerType);

    /**
     * Returns the {@link Channel} that this pipeline is attached to.
     *
     * @return the channel, or {@code null} if this pipeline is not attached yet
     */
    Channel channel();

    /**
     * Returns the names of all {@link ChannelHandler}s in this pipeline.
     *
     * @return the list of names
     */
    List<String> names();

    /**
     * Returns the map of all {@link ChannelHandler}s in this pipeline, with their names as keys.
     *
     * @return the map of handlers
     */
    Map<String, ChannelHandler> toMap();


    // --- Inbound Event Propagation Methods ---

    /**
     * Triggers a {@code channelRegistered} event to the first {@link ChannelInboundHandler} in this pipeline.
     */
    ChannelPipeline fireChannelRegistered();

    /**
     * Triggers a {@code channelUnregistered} event to the first {@link ChannelInboundHandler} in this pipeline.
     */
    ChannelPipeline fireChannelUnregistered();

    /**
     * Triggers a {@code channelActive} event to the first {@link ChannelInboundHandler} in this pipeline.
     */
    ChannelPipeline fireChannelActive();

    /**
     * Triggers a {@code channelInactive} event to the first {@link ChannelInboundHandler} in this pipeline.
     */
    ChannelPipeline fireChannelInactive();

    /**
     * Triggers a {@code channelRead} event with the specified message to the first {@link ChannelInboundHandler}
     * in this pipeline.
     */
    ChannelPipeline fireChannelRead(Object msg);

    /**
     * Triggers a {@code channelReadComplete} event to the first {@link ChannelInboundHandler} in this pipeline.
     */
    ChannelPipeline fireChannelReadComplete();

    /**
     * Triggers a {@code channelWritabilityChanged} event to the first {@link ChannelInboundHandler} in this pipeline.
     */
    ChannelPipeline fireChannelWritabilityChanged();

    /**
     * Triggers a {@code userEventTriggered} event with the specified event object to the first
     * {@link ChannelInboundHandler} in this pipeline.
     */
    ChannelPipeline fireUserEventTriggered(Object event);

    /**
     * Triggers an {@code exceptionCaught} event with the specified cause to the first {@link ChannelHandler}
     * (inbound or outbound) in this pipeline.
     */
    ChannelPipeline fireExceptionCaught(Throwable cause);


    // --- Outbound Operation Methods ---

    /**
     * Requests to bind the {@link Channel} of this pipeline to the specified local address.
     * This action propagates through the pipeline starting from the tail.
     */
    ChannelFuture bind(SocketAddress localAddress);
    ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise);

    /**
     * Requests to connect the {@link Channel} of this pipeline to the specified remote address.
     * This action propagates through the pipeline starting from the tail.
     */
    ChannelFuture connect(SocketAddress remoteAddress);
    ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress);
    ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise);
    ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise);

    /**
     * Requests to disconnect the {@link Channel} of this pipeline from its remote peer.
     * This action propagates through the pipeline starting from the tail.
     */
    ChannelFuture disconnect();
    ChannelFuture disconnect(ChannelPromise promise);

    /**
     * Requests to close the {@link Channel} of this pipeline.
     * This action propagates through the pipeline starting from the tail.
     */
    ChannelFuture close();
    ChannelFuture close(ChannelPromise promise);

    /**
     * Requests to deregister the {@link Channel} of this pipeline from its {@link EventLoop}.
     * This action propagates through the pipeline starting from the tail.
     * (Optional operation, may not be present in all implementations).
     */
    ChannelFuture deregister();
    ChannelFuture deregister(ChannelPromise promise);

    /**
     * Requests to read data from the {@link Channel} of this pipeline.
     * This action propagates through the pipeline starting from the tail.
     */
    ChannelPipeline read();

    /**
     * Requests to write a message through the pipeline.
     * This action propagates through the pipeline starting from the tail.
     */
    ChannelFuture write(Object msg);
    ChannelFuture write(Object msg, ChannelPromise promise);

    /**
     * Requests to flush all pending writes for the {@link Channel} of this pipeline.
     * This action propagates through the pipeline starting from the tail.
     */
    ChannelPipeline flush();

    /**
     * Shortcut for {@code write(msg)} and {@code flush()}.
     * This action propagates through the pipeline starting from the tail.
     */
    ChannelFuture writeAndFlush(Object msg);
    ChannelFuture writeAndFlush(Object msg, ChannelPromise promise);

    // --- Accessors for Head and Tail ---

    /**
     * Returns the first {@link ChannelHandler} in this pipeline.
     * @return the first handler, or {@code null} if the pipeline is empty.
     */
    ChannelHandler first();

    /**
     * Returns the last {@link ChannelHandler} in this pipeline.
     * @return the last handler, or {@code null} if the pipeline is empty.
     */
    ChannelHandler last();

    /**
     * Returns the context of the first {@link ChannelHandler} in this pipeline.
     * @return the context of the first handler, or {@code null} if the pipeline is empty.
     */
    ChannelHandlerContext firstContext();

    /**
     * Returns the context of the last {@link ChannelHandler} in this pipeline.
     * @return the context of the last handler, or {@code null} if the pipeline is empty.
     */
    ChannelHandlerContext lastContext();
}