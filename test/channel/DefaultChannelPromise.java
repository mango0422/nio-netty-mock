package com.example.nionetty.channel;

import com.example.nionetty.eventloop.EventLoop;
import com.example.nionetty.util.concurrent.DefaultPromise; // Needs the base DefaultPromise
import com.example.nionetty.util.concurrent.Future;
import com.example.nionetty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;


/**
 * Default implementation of {@link ChannelPromise}.
 * Extends a base {@link DefaultPromise} and adds {@link Channel} context.
 */
public class DefaultChannelPromise extends DefaultPromise<Void> implements ChannelPromise {

    private static final Logger log = LoggerFactory.getLogger(DefaultChannelPromise.class);

    private final Channel channel;

    /**
     * Creates a new instance.
     *
     * @param channel the {@link Channel} associated with this promise
     */
    public DefaultChannelPromise(Channel channel) {
        // If no executor is provided, how are listeners notified?
        // Need to decide if we enforce passing an executor/eventloop or handle differently.
        // Assuming channel provides the event loop for listener execution.
        super(channel != null ? channel.eventLoop() : null); // Use channel's event loop if available
        this.channel = channel;
    }

    /**
     * Creates a new instance.
     *
     * @param channel   the {@link Channel} associated with this promise
     * @param eventLoop the {@link EventLoop} to execute listeners on
     */
    public DefaultChannelPromise(Channel channel, EventLoop eventLoop) {
        super(eventLoop); // Pass the executor (EventLoop) to the base promise
        this.channel = channel;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    // --- Void Success Methods ---

    @Override
    public ChannelPromise setSuccess() {
        return setSuccess(null); // Call base method with null result
    }

    @Override
    public boolean trySuccess() {
        return trySuccess(null); // Call base method with null result
    }

    // --- Covariant Overrides for Chaining ---

    @Override
    public ChannelPromise setSuccess(Void result) {
        super.setSuccess(result);
        return this;
    }

    @Override
    public ChannelPromise setFailure(Throwable cause) {
        super.setFailure(cause);
        return this;
    }

     @Override
    public ChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        super.addListener(listener);
        return this;
    }

    @Override
    public ChannelPromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        super.removeListener(listener);
        return this;
    }

    @Override
    public ChannelPromise await() throws InterruptedException {
        super.await();
        return this;
    }

    @Override
    public ChannelPromise awaitUninterruptibly() {
        super.awaitUninterruptibly();
        return this;
    }

     @Override
    public ChannelPromise sync() throws InterruptedException, ExecutionException {
        super.sync();
        return this;
    }

    @Override
    public ChannelPromise syncUninterruptibly() throws ExecutionException {
        super.syncUninterruptibly();
        return this;
    }

    // --- Other Methods ---

    @Override
    public ChannelPromise unvoid() {
        // Return a new promise that wraps this one but might prevent modification?
        // Or simply return this if modification isn't a concern in this mock.
        return this; // Simplistic approach
    }

    @Override
    protected void checkDeadLock() {
        // Check if the current thread is the event loop thread associated with this promise
        // to prevent deadlocks when blocking calls like await/sync are made from the loop.
        EventLoop loop = (EventLoop) executor(); // Assuming executor is EventLoop
        if (loop != null && loop.inEventLoop()) {
            throw new BlockingOperationException("Cannot await/sync from within the EventLoop thread");
        }
    }

    @Override
    public String toString() {
        return "DefaultChannelPromise@" + Integer.toHexString(hashCode()) + "(" + super.toString() + ", channel: " + channel + ")";
    }
}

// Helper Exception (can be in its own file or nested)
class BlockingOperationException extends IllegalStateException {
    public BlockingOperationException(String s) {
        super(s);
    }
}