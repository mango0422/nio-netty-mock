package com.example.nionetty.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A special {@link ChannelInboundHandler} which offers an easy way to initialize a {@link Channel}
 * once it was registered to its {@link EventLoop}. Implementations are supposed to implement the
 * {@link #initChannel(Channel)} method, add their custom handlers to the {@link ChannelPipeline},
 * and then this handler will automatically remove itself from the pipeline after initialization.
 *
 * @param <C> A sub-type of {@link Channel}
 */
public abstract class ChannelInitializer<C extends Channel> extends ChannelInboundHandlerAdapter { // Needs adapter class

    private static final Logger log = LoggerFactory.getLogger(ChannelInitializer.class);

    /**
     * This method will be called once the {@link Channel} was registered. After the method returns this instance
     * will be removed from the {@link ChannelPipeline} of the {@link Channel}.
     *
     * @param ch the {@link Channel} which was registered.
     * @throws Exception is thrown if an error occurs. In that case the {@link Channel} will be closed.
     */
    protected abstract void initChannel(C ch) throws Exception;

    @Override
    public final void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        boolean removed = false;
        try {
            initChannel((C) ctx.channel()); // Initialize the channel pipeline
            // Remove self after initialization
            ctx.pipeline().remove(this);
            removed = true;
            log.debug("Successfully initialized channel {} and removed {}", ctx.channel(), this.getClass().getSimpleName());
            // Now fire channelRegistered event to the *next* handler (which was added during initChannel)
             ctx.fireChannelRegistered(); // Propagate event onwards
        } catch (Throwable t) {
            log.error("Failed to initialize channel {} pipeline.", ctx.channel(), t);
            // Close channel on initialization error
            ctx.close();
            // exceptionCaught event is fired by the pipeline automatically on close/error usually
        } finally {
            // Ensure removal even if exception occurs after partial init but before remove call
            if (!removed) {
                 try {
                     ctx.pipeline().remove(this);
                     log.debug("Removed {} after initialization failure for channel {}", this.getClass().getSimpleName(), ctx.channel());
                 } catch (Exception e) {
                     log.warn("Failed to remove {} from pipeline after initialization error.", this.getClass().getSimpleName(), e);
                 }
            }
        }
    }

     // Override other inbound methods just to log or ensure they don't block flow if needed,
     // but since it removes itself after channelRegistered, these shouldn't be called.
     // They are handled by the superclass ChannelInboundHandlerAdapter anyway.
}

// // Placeholders required:
// package com.example.nionetty.channel;
// // Channel, ChannelHandlerContext, ChannelPipeline, EventLoop defined elsewhere
// // Need ChannelInboundHandlerAdapter base class
// public class ChannelInboundHandlerAdapter implements ChannelInboundHandler {
//      @Override public void channelRegistered(ChannelHandlerContext ctx) throws Exception { ctx.fireChannelRegistered(); }
//      @Override public void channelUnregistered(ChannelHandlerContext ctx) throws Exception { ctx.fireChannelUnregistered(); }
//      @Override public void channelActive(ChannelHandlerContext ctx) throws Exception { ctx.fireChannelActive(); }
//      @Override public void channelInactive(ChannelHandlerContext ctx) throws Exception { ctx.fireChannelInactive(); }
//      @Override public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception { ctx.fireChannelRead(msg); }
//      @Override public void channelReadComplete(ChannelHandlerContext ctx) throws Exception { ctx.fireChannelReadComplete(); }
//      @Override public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception { ctx.fireUserEventTriggered(evt); }
//      @Override public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception { ctx.fireChannelWritabilityChanged(); }
//      @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception { ctx.fireExceptionCaught(cause); }
//      @Override public void handlerAdded(ChannelHandlerContext ctx) throws Exception { /* NOOP */ }
//      @Override public void handlerRemoved(ChannelHandlerContext ctx) throws Exception { /* NOOP */ }
// }