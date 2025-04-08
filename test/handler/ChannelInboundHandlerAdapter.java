package com.example.nionetty.handler;

import com.example.nionetty.channel.ChannelConfig;
import com.example.nionetty.channel.ChannelHandlerContext;
import com.example.nionetty.channel.ChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Skeleton implementation of {@link ChannelInboundHandler}. Provides default implementations
 * that simply forward the event to the next handler in the pipeline by calling the corresponding
 * method on the {@link ChannelHandlerContext}. Subclasses should override the methods for the
 * events they are interested in handling.
 */
public class ChannelInboundHandlerAdapter implements ChannelInboundHandler {

    private static final Logger log = LoggerFactory.getLogger(ChannelInboundHandlerAdapter.class);


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
     * {@inheritDoc} Calls {@link ChannelHandlerContext#fireChannelRegistered()} to forward
     * to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelRegistered();
    }

    /**
     * {@inheritDoc} Calls {@link ChannelHandlerContext#fireChannelUnregistered()} to forward
     * to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelUnregistered();
    }

    /**
     * {@inheritDoc} Calls {@link ChannelHandlerContext#fireChannelActive()} to forward
     * to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
    }

    /**
     * {@inheritDoc} Calls {@link ChannelHandlerContext#fireChannelInactive()} to forward
     * to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
    }

    /**
     * {@inheritDoc} Calls {@link ChannelHandlerContext#fireChannelRead(Object)} to forward
     * to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     * Implementations are responsible for releasing the message `msg` if it's reference-counted.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.fireChannelRead(msg);
    }

    /**
     * {@inheritDoc} Calls {@link ChannelHandlerContext#fireChannelReadComplete()} to forward
     * to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelReadComplete();
    }

    /**
     * {@inheritDoc} Calls {@link ChannelHandlerContext#fireUserEventTriggered(Object)} to forward
     * to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        ctx.fireUserEventTriggered(evt);
    }

    /**
     * {@inheritDoc} Calls {@link ChannelHandlerContext#fireChannelWritabilityChanged()} to forward
     * to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelWritabilityChanged();
    }

    /**
     * {@inheritDoc} Calls {@link ChannelHandlerContext#fireExceptionCaught(Throwable)} to forward
     * to the next {@link ChannelHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("Exception caught by default handler, passing through: {}", cause.getMessage(), cause);
        ctx.fireExceptionCaught(cause);
    }
}