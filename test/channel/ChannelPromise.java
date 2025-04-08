package com.example.nionetty.channel;



import com.example.nionetty.util.concurrent.Promise; // Needs the base Promise interface



/**

 * Special {@link Promise} which is writable and notifies listeners related to a {@link Channel}.

 * It extends {@link ChannelFuture} to provide channel-specific context and covariant return types.

 *

 * @see ChannelFuture

 * @see Promise

 */

public interface ChannelPromise extends ChannelFuture, Promise<Void> { // Result type is typically Void for channel ops



    /**

     * Returns the {@link Channel} which is associated with this future.

     */

    @Override

    Channel channel();



    /**

     * Marks this future as a success.

     *

     * @return {@code this} promise

     * @throws IllegalStateException if this future is already completed

     */

    ChannelPromise setSuccess(); // Special void version



    /**

     * Marks this future as a success.

     * Does nothing if the future is already completed.

     *

     * @return {@code true} if the promise was marked as success by this call, {@code false} otherwise.

     */

    boolean trySuccess(); // Special void version



    // --- Override methods to return ChannelPromise for chaining ---



    @Override

    ChannelPromise setSuccess(Void result); // Implement Promise<Void>



    @Override

    ChannelPromise setFailure(Throwable cause);



    @Override

    ChannelPromise addListener(com.example.nionetty.util.concurrent.GenericFutureListener<? extends Future<? super Void>> listener);



    @Override

    ChannelPromise removeListener(com.example.nionetty.util.concurrent.GenericFutureListener<? extends Future<? super Void>> listener);



    @Override

    ChannelPromise await() throws InterruptedException;



    @Override

    ChannelPromise awaitUninterruptibly();



    @Override

    ChannelPromise sync() throws InterruptedException, java.util.concurrent.ExecutionException;



    @Override

    ChannelPromise syncUninterruptibly() throws java.util.concurrent.ExecutionException;



    /**

     * Returns a new {@link ChannelPromise} which is not writable.

     * Useful for situations where you need to expose the future result without allowing modifications.

     */

    ChannelPromise unvoid(); // Netty specific utility method often found

}