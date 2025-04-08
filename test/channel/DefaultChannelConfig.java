package com.example.nionetty.channel;

import java.util.HashMap;
import java.util.Map;

public class DefaultChannelConfig implements ChannelConfig {

    private final Channel channel;
    private final Map<ChannelOption<?>, Object> options = new HashMap<>();
    private int connectTimeoutMillis = 30000; // 기본 30초

    public DefaultChannelConfig(Channel channel) {
        this.channel = channel;
    }
    
    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return options;
    }

    @Override
    public boolean setOptions(Map<ChannelOption<?>, ?> options) {
        boolean success = true;
        for (Map.Entry<ChannelOption<?>, ?> entry : options.entrySet()) {
            if (!setOption(entry.getKey(), entry.getValue())) {
                success = false;
            }
        }
        return success;
    }

    @Override
    public <T> T getOption(ChannelOption<T> option) {
        Object value = options.get(option);
        if (value == null) {
            return null;
        }
        try {
            return option.getClass().cast(value);
        } catch (ClassCastException e) {
            throw new IllegalStateException("Option value type mismatch for: " + option.name());
        }
    }

    @Override
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        options.put(option, value);
        if (option == ChannelOption.CONNECT_TIMEOUT_MILLIS && value instanceof Integer) {
            connectTimeoutMillis = (Integer) value;
        }
        return true;
    }

    @Override
    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    @Override
    public ChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
        return this;
    }
}
