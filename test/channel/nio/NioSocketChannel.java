package com.example.nionetty.channel.nio;

import com.example.nionetty.buffer.CustomBuffer; // Using our custom buffer
import com.example.nionetty.channel.*; // Needs Channel, ChannelConfig, Pipeline etc.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * NIO implementation of a Channel for TCP connections using SocketChannel.
 */
public class NioSocketChannel extends AbstractNioChannel { // Needs AbstractNioChannel

    private static final Logger log = LoggerFactory.getLogger(NioSocketChannel.class);

    private final SocketChannel socketChannel;
    private final ChannelConfig config;
    private final DefaultChannelPipeline pipeline; // Needs DefaultChannelPipeline impl

    private final Queue<Object> writeBufferQueue = new ArrayDeque<>(); // Buffer for pending writes
    private boolean writeInProgress = false;

    /**
     * Constructor used by NioServerSocketChannel when accepting a connection.
     */
    public NioSocketChannel(NioServerSocketChannel parent, SocketChannel acceptedChannel) {
        super(parent); // Pass parent if needed by AbstractNioChannel
        this.socketChannel = acceptedChannel;
        this.config = new DefaultChannelConfig(this); // Needs DefaultChannelConfig
        this.pipeline = new DefaultChannelPipeline(this); // Needs DefaultChannelPipeline
        log.debug("NioSocketChannel created for accepted connection: {}", socketChannel);
    }

    /**
     * Constructor for creating a client channel (not fully implemented here).
     */
     public NioSocketChannel() {
         super(null); // No parent for client channel
         try {
             this.socketChannel = SocketChannel.open();
             this.socketChannel.configureBlocking(false);
             this.config = new DefaultChannelConfig(this);
             this.pipeline = new DefaultChannelPipeline(this);
             log.debug("NioSocketChannel created for client: {}", socketChannel);
         } catch (IOException e) {
             throw new ChannelException("Failed to open SocketChannel", e); // Needs ChannelException
         }
     }

    @Override
    protected SelectableChannel javaChannel() {
        return socketChannel;
    }

    @Override
    public ChannelConfig config() {
        return config;
    }

    @Override
    public ChannelPipeline pipeline() {
        return pipeline;
    }

    @Override
    public boolean isOpen() {
        return socketChannel.isOpen();
    }

    @Override
    public boolean isActive() {
        // Active means open and connected
        return isOpen() && socketChannel.isConnected();
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        // Client sockets usually don't explicitly bind before connecting,
        // but it's possible.
        log.debug("Binding client channel {} to {}", this, localAddress);
        socketChannel.bind(localAddress);
    }

    @Override
    protected ChannelFuture doConnect(SocketAddress remoteAddress, SocketAddress localAddress) {
        // Connect logic for client channels
        DefaultChannelPromise connectPromise = new DefaultChannelPromise(this, eventLoop);
        eventLoop.execute(() -> {
            try {
                if (localAddress != null) {
                    socketChannel.bind(localAddress);
                }
                log.info("Attempting to connect channel {} to {}", this, remoteAddress);
                boolean connected = socketChannel.connect(remoteAddress);

                if (connected) {
                    log.info("Connection established immediately for {}", this);
                    // Connection completed immediately
                    selectionKey().interestOps(SelectionKey.OP_READ); // Interested in reading now
                    pipeline.fireChannelActive(); // Fire active event
                    connectPromise.setSuccess();
                } else {
                    log.debug("Connection attempt pending for {}, registering OP_CONNECT", this);
                    // Connection attempt is in progress, register OP_CONNECT
                    selectionKey().interestOps(SelectionKey.OP_CONNECT);
                    // Store promise to be fulfilled when OP_CONNECT is ready
                    // This needs a way to associate the promise with the key/channel
                    selectionKey().attach(this); // Ensure attachment is correct
                    // Add promise to attachment? Or map in event loop? Simpler: store in channel field?
                    // For now, rely on handleNioConnect to find the promise via the channel instance.
                    // This requires connectPromise to be accessible in handleNioConnect - maybe make it a field?
                     // this.connectPromise = connectPromise; // If AbstractNioChannel has such a field
                }
            } catch (Exception e) {
                log.error("Failed to initiate connection for {}", this, e);
                connectPromise.setFailure(e);
                close(); // Close channel on connection failure
            }
        });
        return connectPromise;
    }

    @Override
    protected void handleNioConnect() {
         // Called by event loop when OP_CONNECT is ready (for client channels)
        SelectionKey key = selectionKey();
        if (!key.isConnectable()) return;

        boolean success = false;
        try {
            success = socketChannel.finishConnect();
            log.info("finishConnect() result: {} for channel {}", success, this);

            if (success) {
                 // Connection successful!
                 // Remove OP_CONNECT interest, add OP_READ interest
                key.interestOps((key.interestOps() & ~SelectionKey.OP_CONNECT) | SelectionKey.OP_READ);

                 // Fulfill the promise associated with the connect operation
                 // Assuming connectPromise is accessible (e.g., field or retrieved based on channel)
                 // if (this.connectPromise != null) { this.connectPromise.setSuccess(); }

                 pipeline.fireChannelActive(); // Fire active event
                 log.info("Channel {} connected successfully to {}", this, remoteAddress());

            } else {
                 // Should not happen according to SocketChannel#finishConnect documentation if key is connectable
                 log.error("finishConnect returned false unexpectedly for {}", this);
                  // Fulfill promise with failure
                 // if (this.connectPromise != null) { this.connectPromise.setFailure(new ConnectException("finishConnect returned false")); }
                 close(); // Close on unexpected failure
            }

        } catch (Exception e) {
             log.error("Failed to finish connection for channel {}", this, e);
             // Fulfill promise with failure
             // if (this.connectPromise != null) { this.connectPromise.setFailure(e); }
            close(); // Close on connection error
        }
    }


    /**
     * Called by the EventLoop when OP_READ is ready.
     */
    @Override
    protected void handleNioRead() {
        final SelectionKey key = selectionKey();
        if (!key.isValid() || !key.isReadable()) {
            return;
        }

        // Allocate a buffer for reading. In real Netty, this uses pooled allocators.
        // Using our CustomBuffer wrapper. Size could be configured.
        ByteBuffer tempNioBuffer = ByteBuffer.allocate(1024); // Example size
        CustomBuffer readBuffer = new CustomBuffer(tempNioBuffer);

        int bytesRead = 0;
        boolean channelClosed = false;

        try {
            bytesRead = socketChannel.read(tempNioBuffer); // Read into the underlying NIO buffer
            log.trace("Read {} bytes from channel {}", bytesRead, this);

        } catch (IOException e) {
            // IOException during read usually means connection closed/reset by peer
            log.debug("IOException during read on channel {}, closing.", this, e);
            channelClosed = true;
            // Don't trigger close() directly here, let the finally block handle it after firing inactive
        }

        if (bytesRead > 0) {
            // Prepare buffer for reading by pipeline handlers
            readBuffer.flip();
            // Pass the read data up the pipeline
            // The pipeline should handle processing, potentially converting bytes to messages
             log.debug("Firing channelRead with buffer: {}", readBuffer);
            pipeline.fireChannelRead(readBuffer); // Pass our CustomBuffer
        } else if (bytesRead < 0) {
            // End of stream reached (peer closed connection gracefully)
             log.debug("Peer closed connection gracefully for channel {}", this);
            channelClosed = true;
        }

        if (channelClosed) {
             // If channel closed during read or end-of-stream detected
             close(); // Close the channel from our side
        }
    }


     /**
     * Called by the EventLoop when OP_WRITE is ready or when trying to write data.
     */
    @Override
    protected void handleNioWrite() {
        final SelectionKey key = selectionKey();
        if (!key.isValid() || !key.isWritable()) {
             log.trace("Key not valid or not writable: {}", key);
            return;
        }

        writeInProgress = true; // Mark write as in progress

        try {
            while (true) {
                Object msg = writeBufferQueue.peek(); // Peek first, don't remove yet
                if (msg == null) {
                    // Write buffer is empty, no more data to write currently.
                    // We are no longer interested in OP_WRITE events for now.
                     log.trace("Write buffer empty, removing OP_WRITE interest for {}", this);
                    key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                    break; // Exit the write loop
                }

                // Assume msg is ByteBuffer or CustomBuffer for simplicity
                ByteBuffer nioBuffer;
                if (msg instanceof CustomBuffer) {
                    nioBuffer = ((CustomBuffer) msg).nioBuffer();
                } else if (msg instanceof ByteBuffer) {
                    nioBuffer = (ByteBuffer) msg;
                } else {
                     log.error("Unsupported message type in write buffer: {}", msg.getClass());
                    writeBufferQueue.poll(); // Discard unsupported message
                    continue;
                }

                 // Ensure buffer is ready for reading (writing to channel)
                 // If it wasn't flipped before adding to queue, do it now? Assume it is.

                 log.trace("Attempting to write {} bytes from buffer {} to {}", nioBuffer.remaining(), nioBuffer, this);
                int writtenBytes = socketChannel.write(nioBuffer);
                 log.trace("Wrote {} bytes to channel {}", writtenBytes, this);

                if (!nioBuffer.hasRemaining()) {
                    // Buffer fully written, remove it from the queue
                    writeBufferQueue.poll();
                     log.trace("Buffer fully written, removed from queue. Queue size: {}", writeBufferQueue.size());
                    // TODO: Notify write promise/future if associated with this msg
                } else {
                    // Buffer partially written. Channel cannot accept more data now.
                    // Keep OP_WRITE interest registered, selector will notify when ready again.
                     log.debug("Partial write ({} bytes). Keeping OP_WRITE interest for {}", writtenBytes, this);
                    break; // Exit write loop, wait for next OP_WRITE event
                }
            }
        } catch (IOException e) {
            // Handle write errors (e.g., connection reset)
            log.error("IOException during write on channel {}, closing.", this, e);
            close(); // Close the channel on write error
            // TODO: Fail any pending write promises
        } finally {
            writeInProgress = false; // Write attempt finished
        }

    }


    /**
     * Called externally (e.g., by handlers) to request writing data.
     * This method adds the data to the queue and potentially triggers handleNioWrite if in event loop,
     * or ensures OP_WRITE is registered if not.
     */
    @Override
    public ChannelFuture write(Object msg) {
        // TODO: Implement write promise/future handling
        DefaultChannelPromise promise = new DefaultChannelPromise(this, eventLoop); // Placeholder

         if (!isOpen()) {
             promise.setFailure(new ClosedChannelException());
             return promise;
         }

         // Ensure this runs in the event loop for thread safety with queue/selector ops
         executeInEventLoop(() -> {
             // TODO: Pipeline should process message before adding to queue (e.g., encode)
             // For now, assume msg is ready (ByteBuffer or CustomBuffer)
             if (msg instanceof CustomBuffer || msg instanceof ByteBuffer) {
                  log.trace("Adding message to write queue for {}. Queue size before: {}", this, writeBufferQueue.size());
                 writeBufferQueue.offer(msg);
                  log.trace("Message added. Queue size after: {}", writeBufferQueue.size());

                 // If a write wasn't already in progress and buffer wasn't empty,
                 // we need to ensure OP_WRITE is set or trigger a write attempt.
                 if (!writeInProgress && writeBufferQueue.size() == 1) { // Trigger only if queue went from 0 to 1
                      final SelectionKey key = selectionKey();
                      if (key != null && key.isValid()) {
                          if ((key.interestOps() & SelectionKey.OP_WRITE) == 0) {
                               log.trace("Registering OP_WRITE interest for {}", this);
                              key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                          }
                          // Optionally, try writing immediately if buffer space might be available
                          // handleNioWrite(); // Careful about recursion/stack depth
                          // Or just rely on selector waking up for OP_WRITE
                      } else {
                          log.warn("Cannot write, selection key invalid for {}", this);
                          writeBufferQueue.poll(); // Remove msg if cannot write
                          promise.setFailure(new ClosedChannelException());
                      }
                  } else {
                      log.trace("Write already in progress or queue not initially empty, OP_WRITE likely set.");
                  }

                 // For now, succeed promise immediately upon adding to queue (incorrect, should be on successful write)
                 promise.setSuccess();

             } else {
                 log.error("Unsupported message type for writing: {}", msg.getClass());
                 promise.setFailure(new IllegalArgumentException("Unsupported message type: " + msg.getClass()));
             }
         });

        return promise;
    }


    @Override
    protected void doClose() throws Exception {
         log.debug("Closing socket channel {}", this);
        socketChannel.close();
         // Clean up write buffer? Fail pending promises?
         Object msg;
         while ((msg = writeBufferQueue.poll()) != null) {
            // TODO: Fail associated promises
         }
    }

    private void executeInEventLoop(Runnable task) {
        if (eventLoop.inEventLoop()) {
            task.run();
        } else {
            eventLoop.execute(task);
        }
    }

    @Override
    public SocketAddress localAddress() {
        try {
            return socketChannel.getLocalAddress();
        } catch (IOException e) {
             log.warn("Failed to get local address", e);
            return null;
        }
    }

    @Override
    public SocketAddress remoteAddress() {
         try {
            return socketChannel.getRemoteAddress();
        } catch (IOException e) {
            log.warn("Failed to get remote address", e);
            return null;
        }
    }


    // // Placeholder classes/interfaces needed:
    // package com.example.nionetty.channel;
    // import java.io.IOException;
    // import java.net.SocketAddress;
    // import java.nio.channels.*;
    // import com.example.nionetty.channel.nio.NioEventLoop;
    // public abstract class AbstractNioChannel implements Channel {
    //      protected SelectionKey selectionKey;
    //      protected NioEventLoop eventLoop;
    //      private final Channel parent;
    //      protected AbstractNioChannel(Channel parent) { this.parent = parent; }
    //      public Channel parent() { return parent; }
    //      public SelectionKey selectionKey() { return selectionKey; }
    //      public void setSelectionKey(SelectionKey key) { this.selectionKey = key; }
    //      public NioEventLoop eventLoop() { return eventLoop; }
    //      public void setEventLoop(NioEventLoop loop) { this.eventLoop = loop; }
    //      public ChannelFuture connect(SocketAddress remoteAddress){ return connect(remoteAddress, null);}
    //      public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) { /* pipeline.fire... */ return doConnect(remoteAddress, localAddress);}
    //      public ChannelFuture bind(SocketAddress localAddress) { /* pipeline.fire... */ DefaultChannelPromise p = new DefaultChannelPromise(this, eventLoop); executeInEventLoop(() -> {try {doBind(localAddress); p.setSuccess();} catch (Exception e) {p.setFailure(e);}}); return p; }
    //      public ChannelFuture close() { /* pipeline.fire... */ DefaultChannelPromise p = new DefaultChannelPromise(this, eventLoop); executeInEventLoop(() -> {try {doClose(); p.setSuccess();} catch (Exception e) {p.setFailure(e);}}); return p; }
    //      public ChannelFuture write(Object msg) { /* pipeline.fire... */ return null; /* Implemented in subclass */ }
    //      protected abstract SelectableChannel javaChannel();
    //      protected abstract void doBind(SocketAddress localAddress) throws Exception;
    //      protected abstract void doClose() throws Exception;
    //      protected ChannelFuture doConnect(SocketAddress remoteAddress, SocketAddress localAddress) { /* Implemented in subclass */ return null;}
    //      protected abstract void handleNioRead();
    //      protected abstract void handleNioWrite();
    //      protected abstract void handleNioConnect();
    //      protected void executeInEventLoop(Runnable task) { if (eventLoop.inEventLoop()) task.run(); else eventLoop.execute(task); }
    //      @Override public String toString() { return this.getClass().getSimpleName() + "[" + javaChannel() + "]"; } // Basic toString
    // }
    // public interface Channel { /* Methods: parent, config, pipeline, isOpen, isActive, localAddress, remoteAddress, bind, connect, close, write ... */ }
    // public interface ChannelConfig { /* Methods: getOption, setOption ... */ }
    // public class DefaultChannelPipeline implements ChannelPipeline { public DefaultChannelPipeline(Channel ch) {} public void fireChannelRegistered(){} public void fireChannelActive(){} public void fireChannelRead(Object msg){} /* ... */ }
    // public class ChannelException extends RuntimeException { public ChannelException(String msg, Throwable cause) { super(msg, cause); } }
    // public class DefaultChannelPromise implements ChannelPromise { public DefaultChannelPromise(Channel ch, EventLoop l) {} /* setSuccess, setFailure... */ }
}