package com.example.nionetty.channel;

/**
 * {@code ChannelHandler} 인터페이스는 채널의 이벤트(데이터 읽기, 쓰기, 연결 등)를 처리하기 위한 기본
 * 핸들러 역할을 정의합니다.
 * <p>
 * 이 인터페이스를 구현함으로써 사용자 정의 채널 핸들러를 작성할 수 있으며,
 * Netty와 유사한 파이프라인 구조 내에서 이벤트 처리 로직을 캡슐화할 수 있습니다.
 * </p>
 *
 * @see ChannelHandlerContext
 * 
 * @author
 * @version 1.0
 */
public interface ChannelHandler {

    /**
     * 핸들러가 채널 파이프라인에 추가될 때 호출됩니다.
     *
     * @param ctx 핸들러 컨텍스트
     * @throws Exception 핸들러 추가 중 발생하는 예외
     */
    default void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // 기본 구현은 아무 것도 하지 않음.
    }

    /**
     * 핸들러가 채널 파이프라인에서 제거될 때 호출됩니다.
     *
     * @param ctx 핸들러 컨텍스트
     * @throws Exception 핸들러 제거 중 발생하는 예외
     */
    default void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // 기본 구현은 아무 것도 하지 않음.
    }
}
