package com.example.nionetty.channel;

/**
 * {@code DefaultChannelPipeline} 클래스는 {@link ChannelPipeline}의 기본 구현체입니다.
 * 별도의 추가 로직 없이 부모 클래스의 기능을 그대로 사용합니다.
 * 
 * @author 
 * @version 1.0
 */
public class DefaultChannelPipeline extends ChannelPipeline {

    /**
     * 생성자로 소속 채널을 받아 파이프라인을 초기화합니다.
     * 
     * @param channel 소속 채널
     */
    public DefaultChannelPipeline(Channel channel) {
        super(channel);
    }
}
