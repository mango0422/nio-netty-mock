package com.example.nionetty.channel.nio;

import com.example.nionetty.channel.EventLoop; // Assuming this interface exists
import com.example.nionetty.channel.EventLoopGroup; // Assuming this interface exists
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages a pool of NioEventLoop instances, each running in its own thread.
 */
public class NioEventLoopGroup implements EventLoopGroup {

    private static final Logger log = LoggerFactory.getLogger(NioEventLoopGroup.class);
    private static final int DEFAULT_THREADS = Runtime.getRuntime().availableProcessors() * 2;

    private final NioEventLoop[] children;
    private final ExecutorService executorService; // Manages the threads for the event loops
    private final AtomicInteger childIndex = new AtomicInteger();
    private final String poolName;


    public NioEventLoopGroup() {
        this(0, "NioEventLoopGroup");
    }

    public NioEventLoopGroup(int nThreads) {
         this(nThreads, "NioEventLoopGroup");
    }

    public NioEventLoopGroup(int nThreads, String poolName) {
        if (nThreads < 0) {
            throw new IllegalArgumentException("nThreads must be >= 0");
        }
        this.poolName = poolName;
        int numThreads = nThreads == 0 ? DEFAULT_THREADS : nThreads;
        log.info("Creating {} with {} threads", poolName, numThreads);

        this.children = new NioEventLoop[numThreads];
        // We need a dedicated thread pool for the event loops
        this.executorService = Executors.newFixedThreadPool(numThreads, runnable -> {
            Thread t = new Thread(runnable);
            t.setName(poolName + "-" + childIndex.getAndIncrement());
            t.setDaemon(false); // Allow JVM to exit if only daemon threads remain? Set based on need.
            return t;
        });

        // Reset index for loop creation
        childIndex.set(0);

        for (int i = 0; i < numThreads; i++) {
            boolean success = false;
            try {
                children[i] = new NioEventLoop(this, executorService); // Pass group and executor
                success = true;
            } catch (IOException e) {
                log.error("Failed to create NioEventLoop", e);
                throw new RuntimeException("Failed to create NioEventLoop", e);
            } finally {
                if (!success) {
                    // Shutdown already created loops and executor if creation fails midway
                    log.error("Failed to initialize {}, shutting down.", poolName);
                    shutdownGracefully();
                }
            }
        }
         log.info("{} initialization complete.", poolName);
    }

    @Override
    public EventLoop next() {
        if (children.length == 0) {
            throw new IllegalStateException("EventLoopGroup has no children");
        }
        // Simple round-robin selection
        return children[Math.abs(childIndex.getAndIncrement() % children.length)];
    }

    @Override
    public void shutdownGracefully() {
        log.info("Shutting down {}...", poolName);
        // Signal each event loop to stop processing and close its selector
        for (NioEventLoop loop : children) {
            if (loop != null) {
                loop.shutdown();
            }
        }
        // Shut down the underlying thread pool
        executorService.shutdown();
         log.info("{} shutdown initiated.", poolName);
        // Optionally wait for termination
        // try {
        //     if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
        //         executorService.shutdownNow();
        //     }
        // } catch (InterruptedException e) {
        //     executorService.shutdownNow();
        //     Thread.currentThread().interrupt();
        // }
    }

     @Override
    public String toString() {
        return poolName + " [threads=" + children.length + "]";
    }

    // // Placeholder Interface needed:
    // package com.example.nionetty.channel;
    // public interface EventLoopGroup { EventLoop next(); void shutdownGracefully(); }
}