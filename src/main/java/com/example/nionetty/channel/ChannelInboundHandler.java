package com.example.nionetty.channel;

/**
 * {@code ChannelInboundHandler} 인터페이스는 인바운드 이벤트(예: 데이터 읽기, 채널 활성화/비활성화, 예외 처리)를 처리하기 위한 메서드들을 정의합니다.
 * <p>
 * 이 인터페이스를 구현하여 커스텀 인바운드 핸들러를 작성할 수 있으며, 채널 파이프라인 내에서 이벤트 흐름을 처리할 수 있습니다.
 * </p>
 *
 * @see ChannelHandler
 * @see ChannelHandlerContext
 * 
 * @author 
 * @version 1.0
 */
public interface ChannelInboundHandler extends ChannelHandler {

    /**
     * 채널이 활성화 되었을 때 호출됩니다.
     *
     * @param ctx 채널 핸들러 컨텍스트
     * @throws Exception 활성화 처리 중 발생할 수 있는 예외
     */
    void channelActive(ChannelHandlerContext ctx) throws Exception;

    /**
     * 채널이 비활성화 되었을 때 호출됩니다.
     *
     * @param ctx 채널 핸들러 컨텍스트
     * @throws Exception 비활성화 처리 중 발생할 수 있는 예외
     */
    void channelInactive(ChannelHandlerContext ctx) throws Exception;

    /**
     * 데이터가 읽혀졌을 때 호출됩니다.
     *
     * @param ctx 채널 핸들러 컨텍스트
     * @param msg 읽은 메시지 객체
     * @throws Exception 데이터 처리 중 발생할 수 있는 예외
     */
    void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception;

    /**
     * 읽기 완료 이벤트가 발생했을 때 호출됩니다.
     *
     * @param ctx 채널 핸들러 컨텍스트
     * @throws Exception 이벤트 처리 중 발생할 수 있는 예외
     */
    void channelReadComplete(ChannelHandlerContext ctx) throws Exception;

    /**
     * 예외가 발생했을 때 호출됩니다.
     *
     * @param ctx   채널 핸들러 컨텍스트
     * @param cause 발생한 예외
     * @throws Exception 예외 처리 중 발생할 수 있는 예외
     */
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;
}
