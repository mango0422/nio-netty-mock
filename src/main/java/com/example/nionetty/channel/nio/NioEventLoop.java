package com.example.nionetty.channel.nio;

import com.example.nionetty.eventloop.EventLoop;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

/**
 * {@code NioEventLoop} 클래스는 NIO 기반의 이벤트 루프를 구현합니다.
 */
public class NioEventLoop implements EventLoop {

    /** NIO 셀렉터 객체 */
    private final Selector selector;

    /**
     * {@code NioEventLoop} 생성자.
     *
     * @throws IOException 셀렉터 초기화 중 발생할 수 있는 예외
     */
    public NioEventLoop() throws IOException {
        this.selector = Selector.open();
    }

    @Override
    public void loop() {
        while (true) {
            try {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }
                Set<java.nio.channels.SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<java.nio.channels.SelectionKey> keyIterator = selectedKeys.iterator();
                while (keyIterator.hasNext()) {
                    java.nio.channels.SelectionKey key = keyIterator.next();
                    // TODO: 각 SelectionKey에 대해 읽기/쓰기/연결 수락 이벤트 처리 로직 구현 필요
                    keyIterator.remove();
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    @Override
    public void shutdown() {
        try {
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 현재 {@link Selector} 객체를 반환합니다.
     *
     * @return 내부 {@link Selector} 객체
     */
    public Selector selector() {
        return selector;
    }
}
