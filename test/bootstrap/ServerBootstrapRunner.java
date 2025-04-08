package com.example.nionetty.bootstrap;

import com.example.nionetty.channel.ChannelFuture;
import com.example.nionetty.channel.ChannelInitializer;
import com.example.nionetty.channel.ChannelPipeline;
import com.example.nionetty.channel.nio.NioEventLoopGroup;
import com.example.nionetty.channel.nio.NioServerSocketChannel;
import com.example.nionetty.channel.nio.NioSocketChannel; // Needed for ChannelInitializer type
import com.example.nionetty.handler.EchoServerHandler;    // Example handler (to be created later)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Main class to start the NIO server using ServerBootstrap.
 */
public class ServerBootstrapRunner {

    private static final Logger log = LoggerFactory.getLogger(ServerBootstrapRunner.class);
    private static final int PORT = 8080;

    public static void main(String[] args) {
        // Configure the server
        // Create boss group (accepts connections) and worker group (handles client connections)
        // For simplicity, we might use 1 thread for boss and CPU cores * 2 for worker, like Netty often does.
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1, "BossGroup");
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(0, "WorkerGroup"); // 0 means default (e.g., CPU cores * 2)

        log.info("Server starting...");
        log.info("Boss Group: {}", bossGroup);
        log.info("Worker Group: {}", workerGroup);


        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup) // Set boss and worker groups
                    .channel(NioServerSocketChannel.class) // Use NIO server socket channel
                    // .option(ChannelOption.SO_BACKLOG, 128) // Server socket options (Requires ChannelOption enum)
                    // .childOption(ChannelOption.SO_KEEPALIVE, true) // Accepted socket options
                    .childHandler(new ChannelInitializer<NioSocketChannel>() { // Handler for accepted connections
                        @Override
                        public void initChannel(NioSocketChannel ch) throws Exception {
                            // Get the pipeline for the newly accepted channel
                            ChannelPipeline pipeline = ch.pipeline();
                            // Add handlers to the pipeline (e.g., decoder, encoder, business logic)
                            // Needs EchoServerHandler defined later
                             pipeline.addLast(new EchoServerHandler());
                            log.debug("Initialized channel: {}", ch);
                        }
                    });

            // Bind and start to accept incoming connections.
            log.info("Binding to port {}", PORT);
            // The bind() method should return a ChannelFuture
            ChannelFuture future = bootstrap.bind(new InetSocketAddress(PORT));

            // Wait until the server socket is closed.
            // In this example, this does not happen, the server runs forever.
            // Need a way to wait for the future to complete and then wait for close.
            future.channel().closeFuture().syncUninterruptibly(); // Needs ChannelFuture and closeFuture implementation

            log.info("Server bind successful. Ready on port {}", PORT);


        } catch (Exception e) {
            log.error("Error starting server", e);
        } finally {
            // Shut down all event loops to terminate all threads.
            log.info("Shutting down server...");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            log.info("Server shut down complete.");
        }
    }
}