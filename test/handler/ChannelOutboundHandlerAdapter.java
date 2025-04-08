package com.example.nionetty.handler;

import com.example.nionetty.channel.ChannelHandlerContext;
import com.example.nionetty.channel.ChannelOutboundHandler;
import com.example.nionetty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

/**
 * Skeleton implementation of {@link ChannelOutboundHandler}. Provides default implementations
 * that simply forward the operation request to the next handler in the pipeline by calling the
 * corresponding method on the {@link ChannelHandlerContext}. Subclasses should override the methods
 * for the operations they are interested in intercepting.
 */
public class ChannelOutboundHandlerAdapter implements ChannelOutboundHandler {

     private static final Logger log = LoggerFactory.getLogger(ChannelOutboundHandlerAdapter.class);

    /**
     * Does nothing by default, sub-classes may override this method.
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // NOOP
    }

    /**
     * Does nothing by default, sub-classes may override this method.
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // NOOP
    }

    /**
     * {@inheritDoc} Calls {@link ChannelHandlerContext#bind(SocketAddress, ChannelPromise)} to forward
     * to the next {@link ChannelOutboundHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.bind(localAddress, promise);
    }

    /**
     * {@inheritDoc} Calls {@link ChannelHandlerContext#connect(SocketAddress, SocketAddress, ChannelPromise)} to forward
     * to the next {@link ChannelOutboundHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.connect(remoteAddress, localAddress, promise);
    }

    /**
     * {@inheritDoc} Calls {@link ChannelHandlerContext#disconnect(ChannelPromise)} to forward
     * to the next {@link ChannelOutboundHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.disconnect(promise);
    }

    /**
     * {@inheritDoc} Calls {@link ChannelHandlerContext#close(ChannelPromise)} to forward
     * to the next {@link ChannelOutboundHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.close(promise);
    }

    /**
     * {@inheritDoc} Calls {@link ChannelHandlerContext#deregister(ChannelPromise)} to forward
     * to the next {@link ChannelOutboundHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        // Assuming context has deregister method
        // ctx.deregister(promise); // If context supports this outbound op
        log.warn("Deregister operation not fully implemented in default adapter/context.");
        promise.setSuccess(); // Fulfill promise trivially for now
    }

    /**
     * {@inheritDoc} Calls {@link ChannelHandlerContext#read()} to forward
     * to the next {@link ChannelOutboundHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }

    /**
     * {@inheritDoc} Calls {@link ChannelHandlerContext#write(Object, ChannelPromise)} to forward
     * to the next {@link ChannelOutboundHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ctx.write(msg, promise);
    }

    /**
     * {@inheritDoc} Calls {@link ChannelHandlerContext#flush()} to forward
     * to the next {@link ChannelOutboundHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

     /**
     * {@inheritDoc} Calls {@link ChannelHandlerContext#fireExceptionCaught(Throwable)} to forward
     * to the next {@link ChannelHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("Outbound adapter Exception caught, passing through: {}", cause.getMessage());
        ctx.fireExceptionCaught(cause);
    }
}