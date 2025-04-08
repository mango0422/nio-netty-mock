package com.example.nionetty.channel;

/**
 * {@code ChannelOutboundHandlerAdapter} 클래스는 {@link ChannelOutboundHandler} 인터페이스의 기본 구현체를 제공합니다.
 * <p>
 * 기본 구현에서는 {@link #write(ChannelHandlerContext, Object)}와 {@link #flush(ChannelHandlerContext)} 메서드에서
 * 받은 이벤트를 다음 핸들러로 전달하도록 되어 있어, 필요에 따라 오버라이드하여 커스텀 동작을 구현할 수 있습니다.
 * </p>
 *
 * @see ChannelOutboundHandler
 * @see ChannelHandlerContext
 * 
 * @author 
 * @version 1.0
 */
public class ChannelOutboundHandlerAdapter implements ChannelOutboundHandler {

    /**
     * 데이터를 기록(write) 요청이 들어왔을 때, 기본적으로 이벤트를 다음 핸들러로 전달합니다.
     *
     * @param ctx 채널 핸들러 컨텍스트
     * @param msg 기록할 메시지 객체
     * @return 기록 작업의 결과를 나타내는 {@link ChannelFuture} 객체
     * @throws Exception 기록 작업 중 발생할 수 있는 예외
     */
    @Override
    public ChannelFuture write(ChannelHandlerContext ctx, Object msg) throws Exception {
        return ctx.write(msg);
    }

    /**
     * 플러시 요청이 들어왔을 때, 기본적으로 이벤트를 다음 핸들러로 전달합니다.
     *
     * @param ctx 채널 핸들러 컨텍스트
     * @throws Exception 플러시 작업 중 발생할 수 있는 예외
     */
    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        // 기본 구현: 특별한 처리 없이 단순히 플러시 이벤트를 전달
        ctx.fireChannelReadComplete();
    }

    /**
     * 핸들러가 채널 파이프라인에 추가될 때 호출되는 기본 구현입니다.
     *
     * @param ctx 채널 핸들러 컨텍스트
     * @throws Exception 핸들러 추가 중 발생할 수 있는 예외
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // 기본 구현: 아무런 작업 수행하지 않음.
    }

    /**
     * 핸들러가 채널 파이프라인에서 제거될 때 호출되는 기본 구현입니다.
     *
     * @param ctx 채널 핸들러 컨텍스트
     * @throws Exception 핸들러 제거 중 발생할 수 있는 예외
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // 기본 구현: 아무런 작업 수행하지 않음.
    }
}
