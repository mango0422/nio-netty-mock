package com.example.nionetty.channel;

import com.example.nionetty.util.concurrent.Future;
import com.example.nionetty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Default implementation of {@link ChannelPipeline}.
 * Manages a doubly-linked list of {@link ChannelHandlerContext}s.
 */
public class DefaultChannelPipeline implements ChannelPipeline {

    private static final Logger log = LoggerFactory.getLogger(DefaultChannelPipeline.class);
    // Weak map to GC handlers early if handlerAdded/Removed propagates exception
    private static final WeakHashMap<Class<?>, String> nameCaches = new WeakHashMap<>();

    private final Channel channel;

    // Head and Tail context nodes for the pipeline structure
    final AbstractChannelHandlerContext head;
    final AbstractChannelHandlerContext tail;

    // Used to propagate exceptions/notifications if handlerAdded/Removed fails
    private boolean firstRegistration = true;

    // Required for Channel#unsafe() delegation
    private MessageSizeEstimator.Handle estimatorHandle;

    public DefaultChannelPipeline(Channel channel) {
        this.channel = channel;

        // Create internal head and tail handlers/contexts
        tail = new TailContext(this);
        head = new HeadContext(this);

        head.next = tail;
        tail.prev = head;
    }

    // --- Context Implementation ---
    // Base class for context nodes in the pipeline's linked list
    abstract static class AbstractChannelHandlerContext implements ChannelHandlerContext {
        volatile AbstractChannelHandlerContext next;
        volatile AbstractChannelHandlerContext prev;

        private final DefaultChannelPipeline pipeline;
        private final String name;
        private final boolean ordered; // For ordered execution guarantee if needed
        private final int executionMask; // Mask for handler type (inbound/outbound)

        // Manages the attachment states, default is ADD_PENDING
        private static final int ADD_PENDING = 0;
        private static final int ADD_COMPLETE = 1;
        private static final int REMOVE_COMPLETE = 2;
        private volatile int handlerState = ADD_PENDING;

        AbstractChannelHandlerContext(DefaultChannelPipeline pipeline, String name,
                                      boolean inbound, boolean outbound) {
            this.name = name;
            this.pipeline = pipeline;
            this.executionMask = (inbound ? MASK_ONLY_INBOUND : 0) | (outbound ? MASK_ONLY_OUTBOUND : 0);
            // ordered = // Determine if the handler requires ordered execution
            this.ordered = false; // Simplified: assume not ordered
        }

        @Override
        public Channel channel() {
            return pipeline.channel();
        }

        @Override
        public ChannelPipeline pipeline() {
            return pipeline;
        }

        @Override
        public EventLoop executor() {
            return channel().eventLoop(); // Delegate to channel's event loop
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public boolean isRemoved() {
             return handlerState == REMOVE_COMPLETE;
        }

         // Placeholder for AttributeMap implementation
        @Override
        public <T> com.example.nionetty.util.Attribute<T> attr(com.example.nionetty.util.AttributeKey<T> key) {
            return channel().attr(key); // Delegate to channel for attributes
        }

        @Override
        public <T> boolean hasAttr(com.example.nionetty.util.AttributeKey<T> key) {
             return channel().hasAttr(key); // Delegate to channel
        }

        // Helper methods for finding next/prev context of specific type
        private AbstractChannelHandlerContext findContextInbound() {
            AbstractChannelHandlerContext ctx = this;
            do {
                ctx = ctx.next;
            } while (!ctx.isInbound());
            return ctx;
        }

        private AbstractChannelHandlerContext findContextOutbound() {
            AbstractChannelHandlerContext ctx = this;
            do {
                ctx = ctx.prev;
            } while (!ctx.isOutbound());
            return ctx;
        }

        // --- Inbound Event Firing ---
        @Override
        public ChannelHandlerContext fireChannelRegistered() {
            invokeChannelRegistered(findContextInbound());
            return this;
        }

        @Override
        public ChannelHandlerContext fireChannelUnregistered() {
            invokeChannelUnregistered(findContextInbound());
            return this;
        }

        @Override
        public ChannelHandlerContext fireChannelActive() {
             invokeChannelActive(findContextInbound());
            return this;
        }

        @Override
        public ChannelHandlerContext fireChannelInactive() {
            invokeChannelInactive(findContextInbound());
            return this;
        }

         @Override
        public ChannelHandlerContext fireChannelRead(Object msg) {
             invokeChannelRead(findContextInbound(), msg);
            return this;
        }

        @Override
        public ChannelHandlerContext fireChannelReadComplete() {
            invokeChannelReadComplete(findContextInbound());
            return this;
        }

         @Override
        public ChannelHandlerContext fireChannelWritabilityChanged() {
            invokeChannelWritabilityChanged(findContextInbound());
            return this;
        }

         @Override
        public ChannelHandlerContext fireUserEventTriggered(Object evt) {
             invokeUserEventTriggered(findContextInbound(), evt);
            return this;
        }

        @Override
        public ChannelHandlerContext fireExceptionCaught(Throwable cause) {
            invokeExceptionCaught(findContextInbound(), cause); // Start search from next inbound
            // Or maybe start from current context and search forward? Netty behavior is complex here.
            // Let's simplify: find next that implements exceptionCaught.
            // invokeExceptionCaught(findContextExceptionCaught(), cause);
            return this;
        }

        // --- Outbound Event Firing (Operations) ---

        @Override
        public ChannelFuture bind(SocketAddress localAddress) {
            return bind(localAddress, newPromise());
        }
        @Override
        public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
            final AbstractChannelHandlerContext next = findContextOutbound();
             // Execute in event loop for thread safety
            EventLoop loop = executor();
            if (loop.inEventLoop()) {
                next.invokeBind(localAddress, promise);
            } else {
                safeExecute(loop, () -> next.invokeBind(localAddress, promise), promise, null);
            }
            return promise;
        }


         @Override
        public ChannelFuture connect(SocketAddress remoteAddress) {
            return connect(remoteAddress, null, newPromise());
        }
        @Override
        public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
             return connect(remoteAddress, localAddress, newPromise());
        }
         @Override
        public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
             return connect(remoteAddress, null, promise);
        }
         @Override
        public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
             final AbstractChannelHandlerContext next = findContextOutbound();
             EventLoop loop = executor();
             if (loop.inEventLoop()) {
                 next.invokeConnect(remoteAddress, localAddress, promise);
             } else {
                 safeExecute(loop, () -> next.invokeConnect(remoteAddress, localAddress, promise), promise, null);
             }
            return promise;
        }


        @Override
        public ChannelFuture disconnect() {
            return disconnect(newPromise());
        }
        @Override
        public ChannelFuture disconnect(ChannelPromise promise) {
            final AbstractChannelHandlerContext next = findContextOutbound();
             EventLoop loop = executor();
            if (loop.inEventLoop()) {
                 if (!channel().metadata().hasDisconnect()) { // Check if transport supports disconnect
                    // Simulate disconnect by closing if not supported
                    next.invokeClose(promise);
                } else {
                    next.invokeDisconnect(promise);
                }
            } else {
                 safeExecute(loop, () -> {
                     if (!channel().metadata().hasDisconnect()) {
                         next.invokeClose(promise);
                     } else {
                         next.invokeDisconnect(promise);
                     }
                 }, promise, null);
            }
            return promise;
        }


         @Override
        public ChannelFuture close() {
            return close(newPromise());
        }
         @Override
        public ChannelFuture close(ChannelPromise promise) {
             final AbstractChannelHandlerContext next = findContextOutbound();
             EventLoop loop = executor();
             if (loop.inEventLoop()) {
                next.invokeClose(promise);
             } else {
                 safeExecute(loop, () -> next.invokeClose(promise), promise, null);
             }
            return promise;
        }

        // Deregister omitted for simplicity initially

        @Override
        public ChannelHandlerContext read() {
             final AbstractChannelHandlerContext next = findContextOutbound();
             EventLoop loop = executor();
             if (loop.inEventLoop()) {
                next.invokeRead();
             } else {
                 // Create a task to invoke read in the event loop
                 Runnable readTask = next::invokeRead;
                 safeExecute(loop, readTask, null, null); // No promise for read()
             }
            return this;
        }


         @Override
        public ChannelFuture write(Object msg) {
            return write(msg, newPromise());
        }
         @Override
        public ChannelFuture write(Object msg, ChannelPromise promise) {
            // TODO: Filter message (e.g., check type, reference counting)
             if (msg == null) { throw new NullPointerException("msg"); }
             final AbstractChannelHandlerContext next = findContextOutbound();
             EventLoop loop = executor();
             if (loop.inEventLoop()) {
                 next.invokeWrite(msg, promise);
             } else {
                 // Handle reference counting if applicable before scheduling
                 // Object W = filterOutboundMessage(msg); // Placeholder
                 safeExecute(loop, () -> next.invokeWrite(msg, promise), promise, msg);
             }
             return promise;
        }


        @Override
        public ChannelHandlerContext flush() {
            final AbstractChannelHandlerContext next = findContextOutbound();
             EventLoop loop = executor();
             if (loop.inEventLoop()) {
                next.invokeFlush();
             } else {
                 Runnable flushTask = next::invokeFlush;
                 safeExecute(loop, flushTask, null, null); // No promise for flush()
             }
            return this;
        }


         @Override
        public ChannelFuture writeAndFlush(Object msg) {
             return writeAndFlush(msg, newPromise());
        }
         @Override
        public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
             // Simplified: just call write then flush
             // Real Netty might optimize this
             write(msg, promise);
             flush(); // Flush is called immediately after write schedules the task
             return promise;
        }

        // --- Invocation Methods (Called by fire* or operation methods) ---
        // These static methods ensure the actual handler method is called within the event loop

        static void invokeChannelRegistered(final AbstractChannelHandlerContext ctx) {
            EventLoop loop = ctx.executor();
            if (loop.inEventLoop()) {
                try {
                    ((ChannelInboundHandler) ctx.handler()).channelRegistered(ctx);
                } catch (Throwable t) {
                     ctx.fireExceptionCaught(t); // Notify of handler exception
                }
            } else {
                 loop.execute(() -> {
                    try {
                        ((ChannelInboundHandler) ctx.handler()).channelRegistered(ctx);
                    } catch (Throwable t) {
                        ctx.fireExceptionCaught(t);
                    }
                });
            }
        }
        static void invokeChannelUnregistered(final AbstractChannelHandlerContext ctx) {
             EventLoop loop = ctx.executor();
             if (loop.inEventLoop()) {
                 try {
                     ((ChannelInboundHandler) ctx.handler()).channelUnregistered(ctx);
                 } catch (Throwable t) {
                     ctx.fireExceptionCaught(t);
                 }
             } else {
                  loop.execute(() -> {
                     try {
                         ((ChannelInboundHandler) ctx.handler()).channelUnregistered(ctx);
                     } catch (Throwable t) {
                         ctx.fireExceptionCaught(t);
                     }
                 });
             }
         }
        static void invokeChannelActive(final AbstractChannelHandlerContext ctx) {
             EventLoop loop = ctx.executor();
             if (loop.inEventLoop()) {
                 try {
                     ((ChannelInboundHandler) ctx.handler()).channelActive(ctx);
                 } catch (Throwable t) {
                     ctx.fireExceptionCaught(t);
                 }
             } else {
                  loop.execute(() -> {
                     try {
                         ((ChannelInboundHandler) ctx.handler()).channelActive(ctx);
                     } catch (Throwable t) {
                         ctx.fireExceptionCaught(t);
                     }
                 });
             }
         }
         static void invokeChannelInactive(final AbstractChannelHandlerContext ctx) {
             EventLoop loop = ctx.executor();
             if (loop.inEventLoop()) {
                 try {
                     ((ChannelInboundHandler) ctx.handler()).channelInactive(ctx);
                 } catch (Throwable t) {
                     ctx.fireExceptionCaught(t);
                 }
             } else {
                 loop.execute(() -> {
                     try {
                         ((ChannelInboundHandler) ctx.handler()).channelInactive(ctx);
                     } catch (Throwable t) {
                         ctx.fireExceptionCaught(t);
                     }
                 });
             }
         }
        static void invokeChannelRead(final AbstractChannelHandlerContext ctx, final Object msg) {
             // TODO: Handle reference counting if msg requires it
             EventLoop loop = ctx