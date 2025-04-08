package com.example.nionetty.util;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents the result of an asynchronous operation.
 * Similar concepts to java.util.concurrent.Future and Netty's Future.
 *
 * @param <V> The result type
 */
public interface Future<V> extends java.util.concurrent.Future<V> { // Extending Java's Future for basic compatibility

    /**
     * Returns {@code true} if and only if the I/O operation was completed successfully.
     */
    boolean isSuccess();

    /**
     * Returns the cause of the failure if the I/O operation has failed.
     *
     * @return the cause of the failure.
     * {@code null} if succeeded or not completed yet.
     */
    Throwable cause();

    /**
     * Adds the specified listener to this future. The specified listener is notified
     * when this future is completed. If this future is already completed, the
     * specified listener is notified immediately.
     */
    Future<V> addListener(GenericFutureListener<? extends Future<? super V>> listener);

    /**
     * Removes the specified listener from this future.
     * The specified listener is no longer notified when this future is completed.
     * If the specified listener is not associated with this future, this method
     * does nothing and returns silently.
     */
    Future<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener);

    /**
     * Waits for this future to be completed.
     *
     * @throws InterruptedException if the current thread was interrupted
     */
    Future<V> await() throws InterruptedException;

    /**
     * Waits for this future to be completed without interruption.
     */
    Future<V> awaitUninterruptibly();

    /**
     * Waits for this future to be completed within the specified time limit.
     *
     * @return {@code true} if and only if the future was completed within the specified time limit
     * @throws InterruptedException if the current thread was interrupted
     */
    boolean await(long timeout, TimeUnit unit) throws InterruptedException;
    boolean await(long timeoutMillis) throws InterruptedException; // Convenience method


    /**
     * Waits for this future to be completed within the specified time limit without interruption.
     *
     * @return {@code true} if and only if the future was completed within the specified time limit
     */
    boolean awaitUninterruptibly(long timeout, TimeUnit unit);
    boolean awaitUninterruptibly(long timeoutMillis); // Convenience method


    /**
     * Waits for this future to be completed and returns the result.
     * If the future completed successfully, the result (possibly {@code null}) is returned.
     * If the future failed, the cause of the failure is thrown.
     *
     * @throws InterruptedException if the current thread was interrupted
     * @throws ExecutionException if the computation threw an exception (retrieved via cause())
     */
    Future<V> sync() throws InterruptedException, ExecutionException;

    /**
     * Waits for this future to be completed without interruption and returns the result.
     * If the future completed successfully, the result (possibly {@code null}) is returned.
     * If the future failed, the cause of the failure is thrown wrapped in an ExecutionException.
     *
     * @throws ExecutionException if the computation threw an exception
     */
    Future<V> syncUninterruptibly() throws ExecutionException;


    /**
     * Returns the result without blocking. If the future is not completed yet, this
     * method returns {@code null}. If the future failed, the cause will be available
     * via {@link #cause()}.
     *
     * @return the result if completed successfully, {@code null} otherwise.
     */
    V getNow();

    /**
     * {@inheritDoc}
     * Overridden to provide a more specific return type.
     */
    @Override
    boolean cancel(boolean mayInterruptIfRunning); // Inherited from java.util.concurrent.Future

    // Inherited: isCancelled(), isDone(), get(), get(long, TimeUnit)
}