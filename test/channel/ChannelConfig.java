package com.example.nionetty.channel;

import java.util.Map;

/**
 * Configuration options for a {@link Channel}.
 */
public interface ChannelConfig {

    /**
     * Returns all options set for this channel configuration.
     */
    Map<ChannelOption<?>, Object> getOptions(); // Needs ChannelOption defined

    /**
     * Sets all configuration options from the provided map.
     */
    boolean setOptions(Map<ChannelOption<?>, ?> options);

    /**
     * Return the value of the given {@link ChannelOption}.
     */
    <T> T getOption(ChannelOption<T> option);

    /**
     * Sets a configuration option for the {@link Channel}.
     *
     * @return {@code true} if the option was set successfully, {@code false} otherwise.
     */
    <T> boolean setOption(ChannelOption<T> option, T value);

    // --- Common options (examples, actual options depend on transport) ---

    /**
     * Gets the connect timeout in milliseconds.
     */
    int getConnectTimeoutMillis();

    /**
     * Sets the connect timeout in milliseconds.
     */
    ChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis);

    // Add other common options like allocator, buffer sizes, socket options (SO_KEEPALIVE, TCP_NODELAY etc via ChannelOption)
}

// // Placeholder required:
// package com.example.nionetty.channel;
// public final class ChannelOption<T> { /* Constant fields for options, name(), valueOf() etc. */ }