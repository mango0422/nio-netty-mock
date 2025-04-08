package com.example.nionetty.channel;

public class ChannelException extends RuntimeException {
    public ChannelException(String message) {
        super(message);
    }
    public ChannelException(String message, Throwable cause) {
        super(message, cause);
    }
}
