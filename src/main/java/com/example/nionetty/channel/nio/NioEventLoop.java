package com.example.nionetty.channel.nio;

import com.example.nionetty.channel.Channel;
import com.example.nionetty.eventloop.EventLoop;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code NioEventLoop} 클래스는 NIO 기반의 이벤트 루프를 구현합니다.
 */
public class NioEventLoop implements EventLoop {

    private static final Logger logger = LoggerFactory.getLogger(NioEventLoop.class);

    /** NIO 셀렉터 객체 */
    private final Selector selector;

    /**
     * {@code NioEventLoop} 생성자.
     *
     * @throws IOException 셀렉터 초기화 중 발생할 수 있는 예외
     */
    public NioEventLoop() throws IOException {
        this.selector = Selector.open();
        logger.debug("Selector 초기화 완료");
    }

    // 새로 추가: 채널 등록 메서드
    public void register(Channel channel) throws IOException {
        if (channel instanceof NioSocketChannel) {
            NioSocketChannel ns = (NioSocketChannel) channel;
            ns.getJavaChannel().configureBlocking(false);
            ns.getJavaChannel().register(selector, SelectionKey.OP_READ, ns);
            logger.debug("채널 등록 완료: {}", ns.id());
        }
    }

    @Override
    public void loop() {
        while (true) {
            try {
                int readyChannels = selector.select(1000);
                if (readyChannels == 0) {
                    continue;
                }
                Set<java.nio.channels.SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<java.nio.channels.SelectionKey> keyIterator = selectedKeys.iterator();
                while (keyIterator.hasNext()) {
                    // java.nio.channels.SelectionKey key = keyIterator.next();
                    // TODO: 각 SelectionKey에 대해 읽기/쓰기/연결 수락 이벤트 처리 로직 구현 필요
                    SelectionKey key = keyIterator.next();
                    if (key.isReadable()) {
                        Object attachment = key.attachment();
                        if (attachment instanceof NioSocketChannel) {
                            NioSocketChannel ns = (NioSocketChannel) attachment;
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            int bytesRead = ns.getJavaChannel().read(buffer);
                            if (bytesRead > 0) {
                                buffer.flip();
                                byte[] data = new byte[buffer.remaining()];
                                buffer.get(data);
                                // 간단히 읽은 데이터를 문자열로 변환 후 파이프라인으로 전달
                                String received = new String(data, StandardCharsets.UTF_8);
                                logger.debug("데이터 읽음: {} (채널: {})", received, ns.id());
                                ns.pipeline().fireChannelRead(received);
                                ns.pipeline().fireChannelReadComplete();
                            } else if (bytesRead < 0) {
                                // 클라이언트 종료 시 채널 닫기
                                logger.warn("채널 종료 감지 (읽기 -1): {}", ns.id());
                                ns.close();
                            }
                        }
                    }
                    keyIterator.remove();
                }
            } catch (IOException e) {
                logger.error("NioEventLoop 오류", e);
                break;
            }
        }
    }

    @Override
    public void shutdown() {
        try {
            selector.close();
            logger.info("NioEventLoop 셀렉터 종료");
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
