package com.example.nionetty.channel;

/**
 * {@code ChannelHandlerContext} 인터페이스는 채널 핸들러와 채널 파이프라인 사이의 중개자 역할을 수행합니다.
 */
public interface ChannelHandlerContext {

    /**
     * 현재 채널 파이프라인에서 다음 핸들러에게 읽기 이벤트를 전달합니다.
     *
     * @param msg 읽은 메시지 객체
     */
    void fireChannelRead(Object msg);

    /**
     * 현재 채널을 통해 데이터를 기록하고 전송합니다.
     *
     * @param msg 전송할 메시지 객체
     * @return 전송 결과를 나타내는 {@link ChannelFuture} 객체
     */
    ChannelFuture write(Object msg);

    /**
     * 현재 채널 파이프라인의 다음 핸들러에게 예외 이벤트를 전달합니다.
     *
     * @param cause 발생한 예외
     */
    void fireExceptionCaught(Throwable cause);

    /**
     * 현재 채널에 연결된 파이프라인을 반환합니다.
     *
     * @return {@link ChannelPipeline} 객체
     */
    ChannelPipeline pipeline();

    /**
     * 현재 채널을 반환합니다.
     *
     * @return 현재 채널 {@link Channel} 객체
     */
    Channel channel();

    /**
     * 읽기 완료 이벤트를 다음 핸들러로 전달합니다.
     */
    void fireChannelReadComplete();
}
