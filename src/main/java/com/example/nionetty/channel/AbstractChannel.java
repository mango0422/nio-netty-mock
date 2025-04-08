package com.example.nionetty.channel;

import java.net.SocketAddress;

/**
 * {@code AbstractChannel} 클래스는 {@link Channel} 인터페이스의 기본 구현을 제공하며,
 * 서버 및 클라이언트 채널의 공통 동작을 캡슐화합니다.
 */
public abstract class AbstractChannel implements Channel {

    /** 채널 고유의 식별자 */
    private final ChannelId id;

    /** 채널 설정 객체 */
    protected final ChannelConfig config;

    /** 채널의 파이프라인 */
    protected final ChannelPipeline pipeline;

    /** 채널이 닫힐 때까지 대기하는 Future 객체 */
    protected final ChannelFuture closeFuture = new ChannelFuture();

    /**
     * 생성자.
     * 하위 클래스는 이 생성자를 통해 기본 채널 ID와 채널 설정 및 파이프라인을 초기화합니다.
     *
     * @param id     이 채널의 고유 식별자
     * @param config 이 채널의 설정 객체
     */
    public AbstractChannel(ChannelId id, ChannelConfig config) {
        this.id = id;
        this.config = config;
        // 기본 파이프라인 초기화 (구체적인 구현체에서 세부 설정 필요)
        this.pipeline = new DefaultChannelPipeline(this);
    }

    /**
     * 채널의 고유 식별자를 반환합니다.
     *
     * @return 채널 식별자 {@link ChannelId}
     */
    @Override
    public ChannelId id() {
        return this.id;
    }

    /**
     * 채널 설정 정보를 반환합니다.
     *
     * @return 채널 설정 {@link ChannelConfig}
     */
    @Override
    public ChannelConfig config() {
        return this.config;
    }

    /**
     * 채널 파이프라인을 반환합니다.
     *
     * @return 채널 파이프라인 {@link ChannelPipeline}
     */
    @Override
    public ChannelPipeline pipeline() {
        return this.pipeline;
    }

    @Override
    public ChannelFuture closeFuture() {
        return closeFuture;
    }

    /**
     * 지정한 로컬 주소에 채널을 바인딩합니다.
     * 구체적인 바인딩 로직은 하위 클래스에서 구현해야 합니다.
     *
     * @param localAddress 로컬 소켓 주소
     * @return 바인딩 결과를 나타내는 {@link ChannelFuture} 객체
     */
    @Override
    public abstract ChannelFuture bind(SocketAddress localAddress);

    /**
     * 채널을 닫고 모든 연결을 종료합니다.
     * 구체적인 종료 로직은 하위 클래스에서 구현해야 합니다.
     *
     * @return 채널 닫기 결과를 나타내는 {@link ChannelFuture} 객체
     */
    @Override
    public abstract ChannelFuture close();
}
