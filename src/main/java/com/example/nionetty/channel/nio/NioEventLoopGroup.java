package com.example.nionetty.channel.nio;

import com.example.nionetty.eventloop.EventLoop;
import com.example.nionetty.eventloop.EventLoopGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code NioEventLoopGroup} 클래스는 여러 개의 {@link NioEventLoop}을 관리하는 이벤트 루프 그룹입니다.
 * <p>
 * 이 클래스는 여러 I/O 작업을 병렬로 처리하기 위해 지정된 개수만큼의 이벤트 루프를 생성하고,
 * 라운드 로빈 방식 등으로 작업을 분배합니다.
 * </p>
 *
 * @see com.example.nionetty.eventloop.EventLoopGroup
 * 
 * @author
 * @version 1.0
 */
public class NioEventLoopGroup implements EventLoopGroup {

    /** 관리하는 이벤트 루프 목록 */
    private final List<EventLoop> eventLoops;

    /** 생성된 이벤트 루프 수 */
    private final int nThreads;

    /**
     * 생성자.
     * 지정한 스레드 수 만큼의 {@link NioEventLoop}을 생성합니다.
     *
     * @param nThreads 생성할 이벤트 루프의 수
     * @throws IOException 이벤트 루프 생성 중 발생할 수 있는 I/O 예외
     */
    public NioEventLoopGroup(int nThreads) throws IOException {
        this.nThreads = nThreads;
        this.eventLoops = new ArrayList<>(nThreads);
        for (int i = 0; i < nThreads; i++) {
            eventLoops.add(new NioEventLoop());
        }
    }

    /**
     * 기본 생성자.
     * 생성할 이벤트 루프의 수는 CPU 코어 수를 기반으로 합니다.
     *
     * @throws IOException 이벤트 루프 생성 중 발생할 수 있는 I/O 예외
     */
    public NioEventLoopGroup() throws IOException {
        this(Runtime.getRuntime().availableProcessors());
    }

    /**
     * 라운드 로빈 방식으로 다음 {@link EventLoop} 객체를 반환합니다.
     * <p>
     * 실제 구현에서는 각 호출 시마다 순환 인덱스를 갱신하여 분배해야 하나,
     * 여기서는 단순 예제로 첫 번째 이벤트 루프를 반환합니다.
     * </p>
     *
     * @return 선택된 {@link EventLoop} 객체
     */
    @Override
    public EventLoop next() {
        return eventLoops.get(0);
    }

    /**
     * 그룹에 속한 모든 이벤트 루프를 정상 종료합니다.
     * <p>
     * 실제 구현에서는 각 이벤트 루프에 종료 신호를 보내고 자원 해제 작업을 수행해야 합니다.
     * </p>
     */
    @Override
    public void shutdownGracefully() {
        // 단순 예제: 이벤트 루프 종료 로직 구현 필요
        for (EventLoop loop : eventLoops) {
            // 예: loop.shutdown();
        }
    }
}
