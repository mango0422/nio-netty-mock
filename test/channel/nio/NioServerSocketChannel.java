package com.example.nionetty.channel.nio;

import com.example.nionetty.channel.*; // Needs Channel, ServerChannel, ChannelPipeline etc.
import com.example.nionetty.eventloop.EventLoop;
import com.example.nionetty.eventloop.EventLoopGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.*;

/**
 * NIO implementation of a ServerChannel that uses ServerSocketChannel.
 */
public class NioServerSocketChannel extends AbstractNioChannel implements ServerChannel { // Needs AbstractNioChannel
                                                                                          // and ServerChannel

    private static final Logger log = LoggerFactory.getLogger(NioServerSocketChannel.class);

    private final ServerSocketChannel serverSocketChannel;
    // Need ChannelConfig, ChannelPipeline etc. defined later
    private final ChannelConfig config;
    private final DefaultChannelPipeline pipeline; // Needs DefaultChannelPipeline impl

    // These are needed by the bootstrap to configure accepted channels
    private volatile EventLoopGroup workerGroup;
    private volatile ChannelHandler childHandler;

    public NioServerSocketChannel() {
        try {
            this.serverSocketChannel = ServerSocketChannel.open();
            this.serverSocketChannel.configureBlocking(false); // Must be non-blocking for Selector
            this.config = new DefaultChannelConfig(this); // Needs DefaultChannelConfig impl
            this.pipeline = new DefaultChannelPipeline(this); // Needs DefaultChannelPipeline impl

            // Server socket pipeline usually has fewer handlers, maybe just an acceptor
            // pipeline.addLast(new Acceptor()); // Example internal handler

            log.debug("NioServerSocketChannel created: {}", serverSocketChannel);

        } catch (IOException e) {
            log.error("Failed to open ServerSocketChannel", e);
            throw new ChannelException("Failed to open a server socket.", e); // Needs ChannelException
        }
    }

    // Called by Bootstrap
    void setWorkerGroup(EventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;
    }

    // Called by Bootstrap
    void setChildHandler(ChannelHandler childHandler) {
        this.childHandler = childHandler;
    }

    @Override
    protected SelectableChannel javaChannel() {
        return serverSocketChannel;
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
        return serverSocketChannel.isOpen();
    }

    @Override
    public boolean isActive() {
        // Active means open and bound
        return isOpen() && serverSocketChannel.socket().isBound();
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        log.debug("Binding {} to {}", this, localAddress);
        Integer backlog = config().getOption(ChannelOption.SO_BACKLOG);
        if (backlog == null) {
            backlog = 128; // 기본 백로그 값 설정
        }
        serverSocketChannel.bind(localAddress, backlog);
        selectionKey().interestOps(SelectionKey.OP_ACCEPT);
        log.info("Channel bound to {}. Ready to accept connections.", localAddress);
    }

    /**
     * Called by the EventLoop when OP_ACCEPT is ready.
     */
    @Override
    protected void handleNioRead() {
        if (!selectionKey().isAcceptable()) {
            return;
        }

        // Accept new connections in a loop until there are no more pending
        while (true) {
            try {
                SocketChannel acceptedNioChannel = serverSocketChannel.accept();
                if (acceptedNioChannel == null) {
                    // No more pending connections
                    break;
                }

                // Configure the accepted channel (non-blocking)
                acceptedNioChannel.configureBlocking(false);

                log.info("Accepted new connection: {}", acceptedNioChannel.getRemoteAddress());

                // Create our NioSocketChannel wrapper for the accepted connection
                NioSocketChannel acceptedChannel = new NioSocketChannel(this, acceptedNioChannel);

                // Apply child options if any (needs implementation in NioSocketChannel/Config)
                // acceptedChannel.config().setOptions(childOptions);

                // Initialize the pipeline using the childHandler provided by the bootstrap
                try {
                    // The ChannelInitializer typically adds user handlers
                    this.childHandler.handlerAdded(acceptedChannel.pipeline().context(this.childHandler)); // Simulate
                                                                                                           // pipeline
                                                                                                           // add
                    // Assuming ChannelInitializer is a ChannelHandler itself
                    // Or more correctly:
                    // ChannelInitializer initializer = (ChannelInitializer) childHandler;
                    // initializer.initChannel(acceptedChannel); // This adds user handlers
                    log.debug("Initializing pipeline for accepted channel: {}", acceptedChannel);
                    // Use a placeholder init method for now if ChannelInitializer is complex
                    initializeChildChannel(acceptedChannel, childHandler);

                } catch (Throwable t) {
                    log.warn("Failed to initialize pipeline for channel: {}", acceptedChannel, t);
                    acceptedChannel.close(); // Close if initialization fails
                    continue;
                }

                // Register the accepted channel with an EventLoop from the workerGroup
                EventLoop childLoop = workerGroup.next();
                childLoop.register(acceptedChannel); // Asynchronous registration

                // Pipeline event? Usually fireChannelRead on the *server* pipeline with the
                // *accepted channel* as msg
                // pipeline.fireChannelRead(acceptedChannel); // Needs pipeline impl

            } catch (ClosedChannelException e) {
                log.warn("ServerSocketChannel closed while accepting.", e);
                break; // Stop accepting if channel is closed
            } catch (IOException e) {
                // Handle accept errors (e.g., too many open files)
                log.error("IOException while accepting connection", e);
                // May need to throttle or stop accepting temporarily
                break;
            } catch (Exception e) {
                log.error("Unexpected error while accepting connection", e);
                break; // Prevent potential infinite loops on unexpected errors
            }
        }
    }

    // Simplified initialization if ChannelInitializer logic is deferred
    private void initializeChildChannel(NioSocketChannel channel, ChannelHandler handler) throws Exception {
        if (handler instanceof ChannelInitializer) {
            ((ChannelInitializer) handler).initChannel(channel);
        } else {
            // Assume it's a simple handler to be added last
            channel.pipeline().addLast(handler);
        }
        // Need to fire registered/active events maybe
        channel.pipeline().fireChannelRegistered();
        channel.pipeline().fireChannelActive();
    }

    @Override
    protected void handleNioWrite() {
        // Server socket doesn't write data, only accepts.
        log.warn("OP_WRITE received on ServerSocketChannel, should not happen.");
        // Clear the OP_WRITE interest if set accidentally
        SelectionKey key = selectionKey();
        if (key != null && key.isValid() && (key.interestOps() & SelectionKey.OP_WRITE) != 0) {
            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
        }
    }

    @Override
    protected void handleNioConnect() {
        // Server socket doesn't connect.
        log.warn("OP_CONNECT received on ServerSocketChannel, should not happen.");
    }

    @Override
    protected void doClose() throws Exception {
        log.debug("Closing server channel {}", this);
        serverSocketChannel.close();
    }

    // --- Methods specific to ServerChannel maybe ---

    // // Placeholder classes/interfaces needed:
    // package com.example.nionetty.channel;
    // import java.io.IOException;
    // import java.net.SocketAddress;
    // import java.nio.channels.*;
    // public interface ServerChannel extends Channel { }
    // public class ChannelException extends RuntimeException { public
    // ChannelException(String msg, Throwable cause) { super(msg, cause); } }
    // public class DefaultChannelConfig implements ChannelConfig { public
    // DefaultChannelConfig(Channel ch) {} /* ... */ @Override public <T> T
    // getOption(ChannelOption<T> option) { return null;} @Override public <T>
    // boolean setOption(ChannelOption<T> option, T value) {return false;}}
    // public class DefaultChannelPipeline implements ChannelPipeline { public
    // DefaultChannelPipeline(Channel ch) {} /* ... */ @Override public
    // ChannelHandlerContext context(ChannelHandler handler) { return null;}
    // @Override public ChannelPipeline addLast(ChannelHandler h) {return this;}
    // @Override public void fireChannelRegistered(){} @Override public void
    // fireChannelActive(){} @Override public void fireChannelRead(Object msg){} }
    // public interface ChannelHandlerContext {}
    // public abstract class ChannelInitializer<C extends Channel> implements
    // ChannelHandler { public abstract void initChannel(C ch) throws Exception;
    // @Override public void handlerAdded(ChannelHandlerContext ctx) throws
    // Exception {}}
    // public abstract class AbstractNioChannel implements Channel { /* Fields:
    // selectionKey, eventLoop ... Methods: javaChannel(), config(), pipeline(),
    // isOpen(), isActive(), doBind(), doClose(), handleNioRead(), handleNioWrite(),
    // handleNioConnect() ... */ protected SelectionKey selectionKey; protected
    // NioEventLoop eventLoop; public SelectionKey selectionKey() {return
    // selectionKey;} public void setSelectionKey(SelectionKey
    // key){this.selectionKey=key;} public void setEventLoop(NioEventLoop
    // loop){this.eventLoop=loop;} public ChannelFuture bind(SocketAddress
    // localAddress){/*bind logic, return future*/ return null;} public
    // ChannelFuture close(){/*close logic*/ return null;} /*...*/ }

}