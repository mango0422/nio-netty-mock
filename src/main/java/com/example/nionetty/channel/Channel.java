package com.example.nionetty.channel;

import java.net.SocketAddress;

/**
 * {@code Channel} 인터페이스는 네트워크 I/O를 위한 기본 추상화를 제공하며,
 * 채널이 수행해야 하는 필수 기능들을 정의합니다.
 * <p>
 * 각 채널은 고유 식별자, 설정 정보, 파이프라인 등과 함께 데이터를 기록하는 기능도 제공합니다.
 * </p>
 */
public interface Channel {

    /**
     * 채널의 고유 식별자를 반환합니다.
     *
     * @return {@link ChannelId} 객체
     */
    ChannelId id();

    /**
     * 채널의 설정 정보를 반환합니다.
     *
     * @return {@link ChannelConfig} 객체
     */
    ChannelConfig config();

    /**
     * 채널에 연결된 파이프라인을 반환합니다.
     *
     * @return {@link ChannelPipeline} 객체
     */
    ChannelPipeline pipeline();

    /**
     * 지정한 로컬 주소에 채널을 바인딩합니다.
     *
     * @param localAddress 로컬 소켓 주소
     * @return 바인딩 결과를 나타내는 {@link ChannelFuture} 객체
     */
    ChannelFuture bind(SocketAddress localAddress);

    /**
     * 채널을 닫고 모든 관련 자원을 해제합니다.
     *
     * @return 채널 닫기 결과를 나타내는 {@link ChannelFuture} 객체
     */
    ChannelFuture close();

    /**
     * 채널에 데이터를 기록(write)하는 메서드.
     *
     * @param msg 전송할 메시지 객체
     * @return 기록 결과를 나타내는 {@link ChannelFuture} 객체
     */
    ChannelFuture write(Object msg);

    /**
     * 채널 종료 후의 Future를 반환합니다.
     * 채널이 닫힐 때까지 대기할 수 있습니다.
     *
     * @return 채널 종료 결과를 나타내는 {@link ChannelFuture} 객체
     */
    ChannelFuture closeFuture();
}
