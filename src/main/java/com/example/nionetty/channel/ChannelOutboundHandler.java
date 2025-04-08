package com.example.nionetty.channel;

/**
 * {@code ChannelOutboundHandler} 인터페이스는 아웃바운드 이벤트(데이터 쓰기, 플러시, 채널 종료 등)를 처리하기 위한 메서드를 정의합니다.
 * <p>
 * 이 인터페이스를 구현하는 핸들러는 채널 파이프라인에서 발생하는 아웃바운드 이벤트를 가로채거나 처리할 수 있습니다.
 * 일반적으로 {@link ChannelOutboundHandlerAdapter}를 확장하여 기본 동작을 제공받고 필요에 따라 오버라이드합니다.
 * </p>
 *
 * @see ChannelHandlerContext
 * @see ChannelOutboundHandlerAdapter
 * 
 * @author 
 * @version 1.0
 */
public interface ChannelOutboundHandler extends ChannelHandler {

    /**
     * 데이터를 채널을 통해 기록(write) 요청할 때 호출됩니다.
     * <p>
     * 핸들러는 전달된 메시지를 기록하거나, 필요에 따라 다른 핸들러로 전달할 수 있습니다.
     * </p>
     *
     * @param ctx 채널 핸들러 컨텍스트
     * @param msg 기록할 메시지 객체
     * @return 기록 작업의 결과를 나타내는 {@link ChannelFuture} 객체
     * @throws Exception 기록 작업 중 발생할 수 있는 예외
     */
    ChannelFuture write(ChannelHandlerContext ctx, Object msg) throws Exception;

    /**
     * 기록된 데이터를 실제 네트워크로 플러시(flush)할 때 호출됩니다.
     * <p>
     * 이 메서드는 기록 작업에 의해 임시로 보관된 데이터를 전송할 필요가 있을 때 사용됩니다.
     * </p>
     *
     * @param ctx 채널 핸들러 컨텍스트
     * @throws Exception 플러시 작업 중 발생할 수 있는 예외
     */
    void flush(ChannelHandlerContext ctx) throws Exception;
}
