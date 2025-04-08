package com.example.nionetty.eventloop;

/**
 * {@code EventLoopGroup} 인터페이스는 여러 {@link EventLoop} 인스턴스를 관리합니다.
 * <p>
 * 이 인터페이스는 작업을 여러 이벤트 루프에 분배하거나,
 * 그룹 내의 모든 이벤트 루프를 종료하는 등의 작업을 수행하기 위한 메서드를 정의합니다.
 * </p>
 * 
 * @see EventLoop
 * 
 * @author 
 * @version 1.0
 */
public interface EventLoopGroup {

    /**
     * 그룹 내의 다음 {@link EventLoop} 인스턴스를 반환합니다.
     * <p>
     * 일반적으로 라운드 로빈 방식 또는 다른 분배 알고리즘을 사용하여 반환합니다.
     * </p>
     *
     * @return 선택된 {@link EventLoop} 인스턴스
     */
    EventLoop next();

    /**
     * 그룹 내의 모든 이벤트 루프를 정상 종료합니다.
     * <p>
     * 각 이벤트 루프에 종료 신호를 보내고 자원을 해제해야 합니다.
     * </p>
     */
    void shutdownGracefully();
}
