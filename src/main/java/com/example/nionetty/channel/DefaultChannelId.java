package com.example.nionetty.channel;

import java.util.UUID;

/**
 * {@code DefaultChannelId} 클래스는 채널의 고유 식별자를 {@code UUID} 기반으로 생성하는 기본 구현체입니다.
 * 짧은 식별자와 전체 식별자 문자열을 제공합니다.
 * 
 * @author 
 * @version 1.0
 */
public final class DefaultChannelId implements ChannelId {

    private final String id;

    /**
     * 기본 생성자로 {@code UUID}를 기반으로 한 식별자를 생성합니다.
     */
    public DefaultChannelId() {
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public String asShortText() {
        return id.substring(0, 8);
    }

    @Override
    public String asLongText() {
        return id;
    }

    @Override
    public String toString() {
        return asLongText();
    }
}
