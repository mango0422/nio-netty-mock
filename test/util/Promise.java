package com.example.nionetty.util;

/**
 * A writable {@link Future}. Provides methods to set the outcome (success or failure)
 * of the asynchronous operation it represents.
 *
 * @param <V> The result type
 */
public interface Promise<V> extends Future<V> {

    /**
     * Marks this future as a success and sets the result.
     *
     * @param result the result of the operation
     * @return {@code this} promise
     * @throws IllegalStateException if this promise is already completed
     */
    Promise<V> setSuccess(V result);

    /**
     * Marks this future as a success and sets the result.
     * Does nothing if the promise is already completed.
     *
     * @param result the result of the operation
     * @return {@code true} if the promise was marked as success by this call, {@code false} otherwise.
     */
    boolean trySuccess(V result);

    /**
     * Marks this future as a failure and sets the cause.
     *
     * @param cause the cause of the failure
     * @return {@code this} promise
     * @throws IllegalStateException if this promise is already completed
     */
    Promise<V> setFailure(Throwable cause);

    /**
     * Marks this future as a failure and sets the cause.
     * Does nothing if the promise is already completed.
     *
     * @param cause the cause of the failure
     * @return {@code true} if the promise was marked as failure by this call, {@code false} otherwise.
     */
    boolean tryFailure(Throwable cause);

    /**
     * Marks this future as uncancellable. Can be useful for promises representing operations
     * that cannot be cancelled once initiated (like registration).
     *
     * @return {@code true} if the promise was successfully marked as uncancellable,
     * {@code false} if it was already done or cancelled.
     */
    boolean setUncancellable();

    // --- Overridden listener methods for covariant return type ---

    @Override
    Promise<V> addListener(GenericFutureListener<? extends Future<? super V>> listener);

    @Override
    Promise<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener);

    @Override
    Promise<V> await() throws InterruptedException;

    @Override
    Promise<V> awaitUninterruptibly();

    @Override
    Promise<V> sync() throws InterruptedException, java.util.concurrent.ExecutionException; // Match sync() return type

    @Override
    Promise<V> syncUninterruptibly() throws java.util.concurrent.ExecutionException; // Match sync() return type
}

// Need GenericFutureListener
// package com.example.nionetty.util;
// import java.util.EventListener;
// public interface GenericFutureListener<F extends Future<?>> extends EventListener {
//     void operationComplete(F future) throws Exception;
// }