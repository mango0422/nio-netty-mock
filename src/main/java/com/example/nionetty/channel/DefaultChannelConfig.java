package com.example.nionetty.channel;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code DefaultChannelConfig} 클래스는 {@link ChannelConfig} 인터페이스의 기본 구현을 제공합니다.
 * 내부적으로 옵션들을 {@code Map}으로 관리합니다.
 * 
 * @author 
 * @version 1.0
 */
public class DefaultChannelConfig implements ChannelConfig {

    private final Map<ChannelOption<?>, Object> options = new HashMap<>();

    @Override
    public <T> void setOption(ChannelOption<T> option, T value) {
        options.put(option, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOption(ChannelOption<T> option) {
        return (T) options.get(option);
    }
}
