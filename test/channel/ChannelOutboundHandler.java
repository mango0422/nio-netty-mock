package com.example.nionetty.channel;

import java.net.SocketAddress;

/**
 * {@link ChannelHandler} which handles outbound I/O events or intercept outbound operations.
 * <p>
 * Outbound events are triggered by I/O requests such as {@link Channel#connect(SocketAddress)},
 * {@link Channel#write(Object)}, or {@link Channel#read()}. These requests typically originate
 * from user code or handlers earlier in the pipeline and propagate from the tail towards the head.
 * <p>
 * Implementations should override the methods for the operations they are interested in intercepting.
 * They must explicitly pass the operation to the next handler in the pipeline (usually via the
 * {@link ChannelHandlerContext}) if they don't want to discard the operation.
 */
public interface ChannelOutboundHandler extends ChannelHandler {

    /**
     * Called once a request to bind the {@link Channel} to a {@code localAddress} is made.
     *
     * @param ctx           the {@link ChannelHandlerContext} which this {@link ChannelOutboundHandler} belongs to
     * @param localAddress  the {@link SocketAddress} to bind to
     * @param promise       the {@link ChannelPromise} to notify once the operation completes
     * @throws Exception    thrown if an error occurs
     */
    void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception;

    /**
     * Called once a request to connect the {@link Channel} to a {@code remoteAddress} is made.
     * An optional {@code localAddress} can be specified for binding before connecting.
     *
     * @param ctx             the {@link ChannelHandlerContext} which this {@link ChannelOutboundHandler} belongs to
     * @param remoteAddress   the {@link SocketAddress} to connect to
     * @param localAddress    the {@link SocketAddress} to bind to, or {@code null} if none
     * @param promise         the {@link ChannelPromise} to notify once the operation completes
     * @throws Exception      thrown if an error occurs
     */
    void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception;

    /**
     * Called once a request to disconnect the {@link Channel} from its remote peer is made.
     *
     * @param ctx           the {@link ChannelHandlerContext} which this {@link ChannelOutboundHandler} belongs to
     * @param promise       the {@link ChannelPromise} to notify once the operation completes
     * @throws Exception    thrown if an error occurs
     */
    void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception;

    /**
     * Called once a request to close the {@link Channel} is made.
     *
     * @param ctx           the {@link ChannelHandlerContext} which this {@link ChannelOutboundHandler} belongs to
     * @param promise       the {@link ChannelPromise} to notify once the operation completes
     * @throws Exception    thrown if an error occurs
     */
    void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception;

    /**
     * Called once a request to deregister the {@link Channel} from its {@link EventLoop} is made.
     * (Note: Less commonly handled directly by user handlers compared to close/disconnect).
     *
     * @param ctx           the {@link ChannelHandlerContext} which this {@link ChannelOutboundHandler} belongs to
     * @param promise       the {@link ChannelPromise} to notify once the operation completes
     * @throws Exception    thrown if an error occurs
     */
    void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception;

    /**
     * Called once a request to initiate a read operation is made. The actual read from the underlying
     * transport might happen later, triggered by the {@link EventLoop}. This method signifies the intent
     * to read.
     *
     * @param ctx           the {@link ChannelHandlerContext} which this {@link ChannelOutboundHandler} belongs to
     * @throws Exception    thrown if an error occurs
     */
    void read(ChannelHandlerContext ctx) throws Exception;

    /**
     * Called once a request to write a message is made. The message should be passed to the next handler
     * in the pipeline if it needs further processing or translation before being written to the transport.
     * The actual write might be buffered and only sent upon calling {@link #flush(ChannelHandlerContext)}.
     *
     * @param ctx           the {@link ChannelHandlerContext} which this {@link ChannelOutboundHandler} belongs to
     * @param msg           the message to write
     * @param promise       the {@link ChannelPromise} to notify once the write operation has potentially completed
     * (may complete earlier than actual network transmission)
     * @throws Exception    thrown if an error occurs
     */
    void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception;

    /**
     * Called once a request to flush pending data previously written via {@link #write(ChannelHandlerContext, Object, ChannelPromise)}
     * is made. This typically triggers the actual sending of buffered data to the underlying transport.
     *
     * @param ctx           the {@link ChannelHandlerContext} which this {@link ChannelOutboundHandler} belongs to
     * @throws Exception    thrown if an error occurs
     */
    void flush(ChannelHandlerContext ctx) throws Exception;
}