package com.example.nionetty.eventloop;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.Set;

/**
 * {@code SingleThreadEventLoop} 클래스는 단일 스레드에서 I/O 이벤트를 처리하는 이벤트 루프를 구현합니다.
 * <p>
 * 이 클래스는 {@link EventLoop} 인터페이스를 구현하며, 내부적으로 {@link Selector}를 사용하여
 * 채널의 I/O 이벤트를 감시하고 처리합니다. 단일 스레드 환경에서 실행되며, 간단한 I/O 이벤트 처리가 필요한 경우에 사용됩니다.
 * </p>
 * <p>
 * 실제 운영 환경에서는 보다 정교한 에러 처리, 태스크 큐 관리 및 스레드 생명 주기 관리가 필요할 수 있습니다.
 * </p>
 *
 * @see EventLoop
 * 
 * @author
 * @version 1.0
 */
public class SingleThreadEventLoop implements EventLoop {

    /** 내부에서 사용하는 NIO {@link Selector} 객체 */
    private final Selector selector;

    /** 이벤트 루프의 실행 상태 플래그 */
    private volatile boolean running;

    /** 이벤트 루프를 실행하는 스레드 */
    private final Thread thread;

    /**
     * {@code SingleThreadEventLoop} 생성자.
     * 내부 {@code Selector}를 초기화하고, 별도의 스레드에서 이벤트 루프를 실행합니다.
     *
     * @throws IOException {@link Selector} 생성 중 발생할 수 있는 I/O 예외
     */
    public SingleThreadEventLoop() throws IOException {
        this.selector = Selector.open();
        this.running = true;
        this.thread = new Thread(this::loop, "SingleThreadEventLoop");
        this.thread.start();
    }

    /**
     * 이벤트 루프를 실행하여 I/O 이벤트를 지속적으로 처리합니다.
     * <p>
     * 이 메서드는 {@code running} 플래그가 {@code true}인 동안 반복하면서,
     * {@link Selector}를 통해 I/O 이벤트를 감시하고, 적절한 핸들러로 이벤트를 전달합니다.
     * 현재 예시에서는 단순하게 선택된 키들을 순회하며, 키 처리 후에는 {@code cancel} 처리를 수행합니다.
     * </p>
     */
    @Override
    public void loop() {
        while (running) {
            try {
                int readyChannels = selector.select(1000); // 1초 대기
                if (readyChannels == 0) {
                    continue;
                }
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    // TODO: 각 SelectionKey에 대해 실제 I/O 이벤트 처리를 구현합니다.
                    // 예) 읽기, 쓰기, 연결 수락 등의 이벤트 처리
                    keyIterator.remove();
                }
            } catch (IOException e) {
                // 실제 구현에서는 로깅 등을 통해 에러를 기록하고 적절히 처리합니다.
                e.printStackTrace();
            }
        }
    }

    /**
     * 이벤트 루프를 종료하고 내부 자원을 해제합니다.
     * <p>
     * 호출 시 {@code running} 플래그를 {@code false}로 설정하고, {@link Selector}를 닫아
     * 이벤트 루프가 안전하게 종료되도록 합니다.
     * </p>
     */
    @Override
    public void shutdown() {
        running = false;
        try {
            selector.close();
        } catch (IOException e) {
            // 실제 구현에서는 에러 로깅 처리 필요
            e.printStackTrace();
        }
    }

    /**
     * 현재 이벤트 루프에서 사용 중인 {@link Selector} 객체를 반환합니다.
     *
     * @return 내부 {@link Selector} 객체
     */
    public Selector selector() {
        return this.selector;
    }
}
