package com.example.nionetty.channel;

import com.example.nionetty.util.concurrent.Future;
import com.example.nionetty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A special {@link ChannelPromise} that cannot be modified (success/failure cannot be set).
 * It is often used for operations where the outcome is implicitly successful or doesn't need
 * explicit notification (e.g., internal pipeline operations). It avoids unnecessary object allocation.
 *
 * Typically, it represents an already completed, successful future.
 */
public final class VoidChannelPromise implements ChannelPromise {

    private static final Logger log = LoggerFactory.getLogger(VoidChannelPromise.class);

    private final Channel channel;
    private final boolean fireException; // Whether to log/fire exceptions on setFailure attempts

    /**
     * Creates a new instance associated with a Channel.
     * Failure attempts will be logged.
     */
    public VoidChannelPromise(Channel channel) {
        this(channel, true);
    }

    /**
     * Creates a new instance.
     * @param channel the associated Channel
     * @param fireException true if exceptions should be logged on failure attempts.
     */
    public VoidChannelPromise(Channel channel, boolean fireException) {
        if (channel == null) {
            throw new NullPointerException("channel");
        }
        this.channel = channel;
        this.fireException = fireException;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    // --- Always Completed Successfully ---

    @Override
    public boolean isSuccess() {
        return true; // Void promises are typically considered successful immediately
    }

    @Override
    public Throwable cause() {
        return null; // No failure cause for a successful promise
    }

    @Override
    public boolean isDone() {
        return true; // Always done
    }

    @Override
    public boolean isCancelled() {
        return false; // Cannot be cancelled
    }

    @Override
    public Void getNow() {
        return null; // Result of a Void future is null
    }

     @Override
    public Void get() throws InterruptedException, ExecutionException {
        return null; // Already complete, return null
    }

    @Override
    public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null; // Already complete, return null
    }

    // --- Modification Attempts (Ignored or Logged) ---

    @Override
    public ChannelPromise setSuccess(Void result) {
        // Ignore
        return this;
    }

    @Override
    public ChannelPromise setSuccess() {
        // Ignore
        return this;
    }

    @Override
    public boolean trySuccess(Void result) {
        return false; // Cannot be set again
    }

    @Override
    public boolean trySuccess() {
        return false; // Cannot be set again
    }

    @Override
    public ChannelPromise setFailure(Throwable cause) {
        if (fireException) {
            log.warn("Tried to fail a VoidChannelPromise, ignoring: {}", channel, cause);
            // In real Netty, might fire exceptionCaught in pipeline
            // channel.pipeline().fireExceptionCaught(cause);
        }
        return this;
    }

    @Override
    public boolean tryFailure(Throwable cause) {
        if (fireException) {
             log.warn("Tried to fail a VoidChannelPromise, ignoring: {}", channel, cause);
             // channel.pipeline().fireExceptionCaught(cause);
        }
        return false; // Cannot be set
    }

    @Override
    public boolean setUncancellable() {
        return true; // Already effectively uncancellable and done
    }

     @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false; // Cannot be cancelled
    }


    // --- Listener Methods ---

    @Override
    public ChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        // Notify listener immediately as it's already complete
        notifyListener(listener);
        return this;
    }

    @Override
    public ChannelPromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        // No listeners are stored, so nothing to remove
        return this;
    }

    private void notifyListener(GenericFutureListener listener) {
        // Execute listener in event loop if possible, otherwise current thread
        EventLoop loop = channel.eventLoop();
        if (loop != null && loop.inEventLoop()) {
            try {
                listener.operationComplete(this);
            } catch (Throwable t) {
                log.warn("An exception was thrown by {}.operationComplete()", listener.getClass().getName(), t);
            }
        } else if (loop != null) {
             loop.execute(() -> {
                 try {
                     listener.operationComplete(this);
                 } catch (Throwable t) {
                     log.warn("An exception was thrown by {}.operationComplete()", listener.getClass().getName(), t);
                 }
             });
        } else {
             // Fallback: Execute in current thread if no loop available (should be rare)
            try {
                listener.operationComplete(this);
            } catch (Throwable t) {
                log.warn("An exception was thrown by {}.operationComplete() in calling thread", listener.getClass().getName(), t);
            }
        }
    }

    // --- Wait Methods (Return Immediately) ---

    @Override
    public ChannelPromise await() throws InterruptedException {
        // Already complete, no waiting needed
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        return this;
    }

    @Override
    public ChannelPromise awaitUninterruptibly() {
        // Already complete
        return this;
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        return true; // Completed immediately
    }

     @Override
    public boolean await(long timeoutMillis) throws InterruptedException {
         if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        return true;
    }

    @Override
    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
        return true; // Completed immediately
    }

     @Override
    public boolean awaitUninterruptibly(long timeoutMillis) {
        return true;
    }


    // --- Sync Methods (Return Immediately) ---

    @Override
    public ChannelPromise sync() throws InterruptedException, ExecutionException {
         if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        // No exception as it's successful
        return this;
    }

    @Override
    public ChannelPromise syncUninterruptibly() {
         // No exception as it's successful
        return this;
    }

    @Override
    public ChannelPromise unvoid() {
        // Return a regular promise that is already successful?
        // Or just return this, as it behaves like a completed promise.
        return this;
    }

    @Override
    public String toString() {
        return "VoidChannelPromise@" + Integer.toHexString(hashCode()) + "(" + channel + ')';
    }
}