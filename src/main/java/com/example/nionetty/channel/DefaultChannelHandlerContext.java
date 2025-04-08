package com.example.nionetty.channel;

/**
 * {@code DefaultChannelHandlerContext} 클래스는 {@link ChannelHandlerContext}의 기본
 * 구현체입니다.
 */
public class DefaultChannelHandlerContext implements ChannelHandlerContext {

    private final ChannelPipeline pipeline;
    private final ChannelHandler handler;
    private final Channel channel;

    public DefaultChannelHandlerContext(ChannelPipeline pipeline, ChannelHandler handler) {
        this.pipeline = pipeline;
        this.handler = handler;
        this.channel = pipeline.channel();
    }

    @Override
    public void fireChannelRead(Object msg) {
        // 다음 핸들러에 이벤트 전달 (단순 예제)
        // 실제 구현에서는 순서를 관리해야 합니다.
    }

    @Override
    public ChannelFuture write(Object msg) {
        // 이제 Channel 인터페이스에 write(msg)가 정의되어 있으므로 호출 가능
        return channel.write(msg);
    }

    @Override
    public void fireExceptionCaught(Throwable cause) {
        // 다음 핸들러에 예외 전달 (단순 예제)
        cause.printStackTrace();
    }

    @Override
    public ChannelPipeline pipeline() {
        return pipeline;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public void fireChannelReadComplete() {
        // 다음 핸들러에 읽기 완료 이벤트 전달 (단순 예제)
    }
}
