package com.example.nionetty.bootstrap;

import com.example.nionetty.channel.*;
import com.example.nionetty.channel.nio.NioEventLoop;
import com.example.nionetty.channel.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A Bootstrap that makes it easy to bootstrap a server Channel.
 * Mimics Netty's ServerBootstrap for configuration.
 */
public class ServerBootstrap {

    private static final Logger log = LoggerFactory.getLogger(ServerBootstrap.class);

    private volatile EventLoopGroup bossGroup;
    private volatile EventLoopGroup workerGroup;
    private volatile Class<? extends ServerChannel> channelClass; // Use ServerChannel interface
    private volatile ChannelHandler childHandler; // Handler for accepted channels
    private final Map<ChannelOption<?>, Object> options = new HashMap<>(); // Server options
    private final Map<ChannelOption<?>, Object> childOptions = new HashMap<>(); // Child options

    public ServerBootstrap() {
        // Default constructor
    }

    /**
     * Specify the EventLoopGroup that will be used for the ServerChannel and the accepted Channels.
     */
    public ServerBootstrap group(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        this.bossGroup = Objects.requireNonNull(bossGroup, "bossGroup");
        this.workerGroup = Objects.requireNonNull(workerGroup, "workerGroup");
        log.debug("Set bossGroup: {}, workerGroup: {}", bossGroup, workerGroup);
        return this;
    }

    /**
     * The Class which is used to create Channel instances.
     */
    public ServerBootstrap channel(Class<? extends ServerChannel> channelClass) {
        this.channelClass = Objects.requireNonNull(channelClass, "channelClass");
        log.debug("Set channelClass: {}", channelClass.getSimpleName());
        return this;
    }

    /**
     * Set the ChannelHandler which is used to serve the requests for the Channels created by the server.
     */
    public ServerBootstrap childHandler(ChannelHandler childHandler) {
        this.childHandler = Objects.requireNonNull(childHandler, "childHandler");
        log.debug("Set childHandler: {}", childHandler.getClass().getSimpleName());
        return this;
    }

    /**
     * Allow to specify a ChannelOption which is used for the ServerChannel instances.
     */
    public <T> ServerBootstrap option(ChannelOption<T> option, T value) {
        Objects.requireNonNull(option, "option");
        // Value can be null
        this.options.put(option, value);
        log.debug("Set option: {} = {}", option.name(), value);
        return this;
    }

    /**
     * Set the ChannelOptions which will be used for the Channels accepted by the ServerChannel.
     */
    public <T> ServerBootstrap childOption(ChannelOption<T> childOption, T value) {
        Objects.requireNonNull(childOption, "childOption");
        this.childOptions.put(childOption, value);
        log.debug("Set childOption: {} = {}", childOption.name(), value);
        return this;
    }


    /**
     * Create a new Channel and bind it.
     */
    public ChannelFuture bind(SocketAddress localAddress) {
        Objects.requireNonNull(localAddress, "localAddress");
        validate(); // Ensure required fields are set

        // Create the server channel instance (e.g., NioServerSocketChannel)
        ServerChannel serverChannel = initAndRegister();

        // Bind the channel
        // This needs to be asynchronous and return a future
        ChannelFuture bindFuture = serverChannel.bind(localAddress);

        log.info("Binding channel {} to address {}", serverChannel, localAddress);

        // Return the future associated with the bind operation
        return bindFuture;
        // In a real implementation, you'd chain listeners to this future
    }

    private void validate() {
        if (bossGroup == null) {
            throw new IllegalStateException("bossGroup not set");
        }
        if (workerGroup == null) {
            throw new IllegalStateException("workerGroup not set");
        }
        if (channelClass == null) {
            throw new IllegalStateException("channel or channelFactory not set");
        }
        if (childHandler == null) {
            throw new IllegalStateException("childHandler not set");
        }
    }


    private ServerChannel initAndRegister() {
        ServerChannel channel = null;
        try {
            // Reflectively create the channel instance (e.g., NioServerSocketChannel)
             channel = channelClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create channel: " + channelClass, e);
        }

        // Initialize the server channel (set up pipeline, options, etc.)
        // The channel needs access to the workerGroup and the childHandler/childOptions
        // Assuming the Channel has an init method or similar mechanism
        // This part heavily depends on the Channel/Pipeline implementation details.

        // For NioServerSocketChannel, it needs the workerGroup to assign EventLoops to accepted channels
        // and the childHandler to initialize the accepted channels' pipelines.
        if (channel instanceof NioServerSocketChannel) {
             ((NioServerSocketChannel) channel).setChildHandler(childHandler);
             ((NioServerSocketChannel) channel).setWorkerGroup(workerGroup);
             // Apply server options
             // channel.config().setOptions(options); // Needs ChannelConfig implementation
        } else {
            // Handle other channel types if necessary
            log.warn("Channel {} type specific initialization might be missing.", channel.getClass().getSimpleName());
        }

        // Register the channel with an EventLoop from the bossGroup
        // This needs to be done *on* the EventLoop thread for thread safety
        EventLoop eventLoop = bossGroup.next();
        ChannelFuture regFuture = eventLoop.register(channel);

        // Wait or handle the registration completion if necessary (sync/async)
         regFuture.syncUninterruptibly(); // Simple blocking wait for registration

        log.info("Initialized and registered server channel: {}", channel);
        return channel;
    }

}
// Placeholder Interfaces/Classes needed for ServerBootstrap:
// package com.example.nionetty.channel;
// public interface EventLoopGroup { EventLoop next(); void shutdownGracefully(); }
// public interface EventLoop { ChannelFuture register(Channel channel); void execute(Runnable task); /* ... */ }
// public interface ServerChannel extends Channel { /* server specific methods? */ }
// public interface Channel { ChannelFuture bind(SocketAddress addr); ChannelPipeline pipeline(); ChannelConfig config(); ChannelFuture closeFuture(); /* ... */ }
// public interface ChannelHandler {} // Marker interface
// public interface ChannelFuture { Channel channel(); ChannelFuture syncUninterruptibly(); /* ... */ }
// public interface ChannelPipeline { ChannelPipeline addLast(ChannelHandler handler); /* ... */ }
// public interface ChannelConfig { <T> boolean setOption(ChannelOption<T> option, T value); /* ... */ }
// public final class ChannelOption<T> { public static final ChannelOption<Integer> SO_BACKLOG = null; /* ... */ public String name() {return "";} }