package com.example.nionetty.channel;

import java.net.SocketOption; // For standard socket options

/**
 * Represents a configuration option for a {@link Channel} or its underlying transport.
 * This class provides type safety for channel options. Constants for common options
 * should be defined here.
 *
 * @param <T> the type of the option's value
 */
public final class ChannelOption<T> {

    // --- Example Common Channel Options ---

    /**
     * Corresponds to {@link java.net.StandardSocketOptions#SO_RCVBUF}
     */
    public static final ChannelOption<Integer> SO_RCVBUF = valueOf("SO_RCVBUF");

    /**
     * Corresponds to {@link java.net.StandardSocketOptions#SO_SNDBUF}
     */
    public static final ChannelOption<Integer> SO_SNDBUF = valueOf("SO_SNDBUF");

    /**
     * Corresponds to {@link java.net.StandardSocketOptions#TCP_NODELAY}
     */
    public static final ChannelOption<Boolean> TCP_NODELAY = valueOf("TCP_NODELAY");

    /**
     * Corresponds to {@link java.net.StandardSocketOptions#SO_KEEPALIVE}
     */
    public static final ChannelOption<Boolean> SO_KEEPALIVE = valueOf("SO_KEEPALIVE");

    /**
     * Corresponds to {@link java.net.StandardSocketOptions#SO_REUSEADDR}
     */
    public static final ChannelOption<Boolean> SO_REUSEADDR = valueOf("SO_REUSEADDR");

    /**
     * Corresponds to {@link java.net.StandardSocketOptions#SO_LINGER}
     */
    public static final ChannelOption<Integer> SO_LINGER = valueOf("SO_LINGER");

    /**
     * Corresponds to {@link java.net.StandardSocketOptions#IP_TOS}
     */
    public static final ChannelOption<Integer> IP_TOS = valueOf("IP_TOS");

    /**
     * Represents the backlog for server socket channels (maximum queue length for incoming connections).
     * Corresponds to the {@code backlog} parameter in {@link java.net.ServerSocket#bind(SocketAddress, int)}.
     */
    public static final ChannelOption<Integer> SO_BACKLOG = valueOf("SO_BACKLOG");

    /**
     * Option to enable/disable auto-reading data from the channel.
     * If true, the channel will attempt to read data whenever it becomes available.
     * If false, user must explicitly call {@link Channel#read()} or {@link ChannelHandlerContext#read()}.
     */
    public static final ChannelOption<Boolean> AUTO_READ = valueOf("AUTO_READ");

     /**
     * Connect timeout in milliseconds. How long to wait for a connection attempt to succeed.
     */
    public static final ChannelOption<Integer> CONNECT_TIMEOUT_MILLIS = valueOf("CONNECT_TIMEOUT_MILLIS");


    // --- Implementation Details ---

    private final String name;

    private ChannelOption(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        this.name = name;
    }

    /**
     * Creates a new ChannelOption with the given name.
     * Use the static constants for standard options where possible.
     */
    public static <T> ChannelOption<T> valueOf(String name) {
        // In a real implementation, this might use a pool or registry
        // to ensure uniqueness and potentially provide validation.
        return new ChannelOption<>(name);
    }

    /**
     * Returns the name of this option.
     */
    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChannelOption<?> that = (ChannelOption<?>) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name();
    }
}