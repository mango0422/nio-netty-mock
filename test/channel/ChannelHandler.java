package com.example.nionetty.channel;

/**
 * Handles an I/O event or intercepts an I/O operation, and forwards it to the next handler in
 * its {@link ChannelPipeline}.
 * <p>
 * This is a marker interface. Sub-interfaces {@link ChannelInboundHandler} and
 * {@link ChannelOutboundHandler} define specific event handling methods.
 */
public interface ChannelHandler {

    /**
     * Gets called after the {@link ChannelHandler} was added to the actual context and is ready to handle events.
     */
    void handlerAdded(ChannelHandlerContext ctx) throws Exception;

    /**
     * Gets called after the {@link ChannelHandler} was removed from the actual context and it doesn't handle events anymore.
     */
    void handlerRemoved(ChannelHandlerContext ctx) throws Exception;

    /**
     * Gets called if a {@link Throwable} was thrown by an upstream handler's event processing method or by an I/O error.
     */
    @SuppressWarnings("deprecation") // Explicitly acknowledge deprecated method if overridden
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;

}

// // Placeholder required:
// package com.example.nionetty.channel;
// public interface ChannelHandlerContext { /* ... */ }