package com.example.nionetty.channel;

import java.io.Serializable;

/**
 * Represents the unique identifier of a {@link Channel}.
 * <p>
 * The identifier should be unique within the context of the running JVM.
 * It's useful for tracking and managing channels. Implementations should
 * provide meaningful {@code toString()} and comparison logic.
 */
public interface ChannelId extends Serializable, Comparable<ChannelId> {

    /**
     * Returns the short but globally unique string representation of the {@link ChannelId}.
     * Suitable for logging but may not be human-friendly.
     */
    String asShortText();

    /**
     * Returns the long yet globally unique string representation of the {@link ChannelId}.
     * This representation might be more human-readable but potentially longer.
     */
    String asLongText();
}