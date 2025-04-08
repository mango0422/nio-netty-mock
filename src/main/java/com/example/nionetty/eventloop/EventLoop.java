package com.example.nionetty.eventloop;

/**
 * {@code EventLoop} 인터페이스는 채널의 I/O 이벤트를 처리하는 루프를 정의합니다.
 * <p>
 * 이벤트 루프는 {@link java.nio.channels.Selector}를 활용하여 등록된 채널의 I/O 이벤트를 감지하고,
 * 해당 이벤트를 적절히 처리하는 로직을 구현합니다.
 * </p>
 *
 * @see EventLoopGroup
 * 
 * @author 
 * @version 1.0
 */
public interface EventLoop {

    /**
     * 이벤트 루프를 실행하여 I/O 이벤트를 처리합니다.
     * <p>
     * 이 메서드는 보통 별도의 스레드에서 호출되며,
     * 루프 내에서 지속적으로 이벤트를 감시하고 처리합니다.
     * </p>
     */
    void loop();

    /**
     * 이벤트 루프를 종료하고 자원을 해제합니다.
     * <p>
     * 실제 구현에서는 종료 신호를 받고, 안전하게 모든 작업을 마친 후 반환해야 합니다.
     * </p>
     */
    void shutdown();
}
