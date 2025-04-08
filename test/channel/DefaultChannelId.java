package com.example.nionetty.channel;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Default implementation of {@link ChannelId}.
 * Uses a combination of machine ID, process ID, timestamp, sequence number, etc.
 * For simplicity here, we'll use a simple atomic counter.
 */
public final class DefaultChannelId implements ChannelId {

    private static final long serialVersionUID = 1L; // Example UID
    private static final AtomicLong counter = new AtomicLong(0);

    private final String longValue;
    private final String shortValue;
    private final int hashCode;

    // In a real implementation, you might include MAC address, process ID, timestamp etc.
    // private static final byte[] MACHINE_ID = ...;
    // private static final int PROCESS_ID = ...;

    public DefaultChannelId() {
        long id = counter.getAndIncrement();
        // Simple unique string based on counter for this example
        this.shortValue = Long.toHexString(id);
        this.longValue = "channel-id-" + shortValue;
        this.hashCode = (int) (id ^ (id >>> 32)); // Simple hash based on counter
    }

    @Override
    public String asShortText() {
        return shortValue;
    }

    @Override
    public String asLongText() {
        return longValue;
    }

    @Override
    public int compareTo(ChannelId o) {
        if (this == o) {
            return 0;
        }
        if (o instanceof DefaultChannelId) {
            // Compare based on short value (hex strings)
            return this.shortValue.compareTo(((DefaultChannelId) o).shortValue);
        }
        // Fallback to comparing long text representations
        return this.asLongText().compareTo(o.asLongText());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DefaultChannelId that = (DefaultChannelId) obj;
        // Fast check with hashcode before comparing strings
        return this.hashCode == that.hashCode && this.shortValue.equals(that.shortValue);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return asShortText(); // Default toString is the short representation
    }
}