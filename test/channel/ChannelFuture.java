package com.example.nionetty.channel;

import com.example.nionetty.util.concurrent.Future; // Assuming a base Future interface
import com.example.nionetty.util.concurrent.GenericFutureListener; // Assuming listener interface

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * The result of an asynchronous {@link Channel} I/O operation.
 * Provides methods to check completion status, wait for completion, and attach listeners.
 */
public interface ChannelFuture extends Future<Void> { // Extends a base Future<Void> (operation result is usually Void)

    /**
     * Returns the {@link Channel} which is associated with this future.
     */
    Channel channel();

    @Override
    ChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener);

    @Override
    ChannelFuture removeListener(GenericFutureListener<? extends Future<? super Void>> listener);

    @Override
    ChannelFuture await() throws InterruptedException;

    @Override
    ChannelFuture awaitUninterruptibly();

    @Override
    ChannelFuture sync() throws InterruptedException;

    @Override
    ChannelFuture syncUninterruptibly();

    // --- Additional methods specific to ChannelFuture ---

    /**
     * Returns {@code true} if the I/O operation associated with this future has completed successfully.
     */
    @Override
    boolean isSuccess(); // Overridden to be more specific than Future's general completion

    /**
     * Returns {@code true} if this future is cancellable. Not typically used for ChannelFutures.
     */
    @Override
    boolean isCancellable(); // From Future interface

    /**
     * Returns the cause of the failure if the I/O operation has failed.
     *
     * @return the cause of the failure.
     * {@code null} if succeeded or not completed yet.
     */
    @Override
    Throwable cause();

}

// // Placeholders required:
// package com.example.nionetty.util.concurrent;
// import java.util.concurrent.TimeUnit;
// import java.util.concurrent.TimeoutException;
// // Base Future interface (inspired by Netty's/java.util.concurrent.Future)
// public interface Future<V> {
//     boolean isSuccess();
//     boolean isCancellable();
//     Throwable cause();
//     Future<V> addListener(GenericFutureListener<? extends Future<? super V>> listener);
//     Future<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener);
//     Future<V> await() throws InterruptedException;
//     boolean await(long timeout, TimeUnit unit) throws InterruptedException;
//     Future<V> awaitUninterruptibly();
//     boolean awaitUninterruptibly(long timeout, TimeUnit unit);
//     Future<V> sync() throws InterruptedException; // Waits and throws exception if failed
//     Future<V> syncUninterruptibly();
//     boolean isDone();
//     V getNow(); // Get result without blocking, null if not done/failed
//     boolean cancel(boolean mayInterruptIfRunning);
//     // V get() throws InterruptedException, ExecutionException; // From java.util.concurrent.Future
//     // V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException; // From java.util.concurrent.Future
// }
// // Listener interface
// public interface GenericFutureListener<F extends Future<?>> extends java.util.EventListener {
//     void operationComplete(F future) throws Exception;
// }