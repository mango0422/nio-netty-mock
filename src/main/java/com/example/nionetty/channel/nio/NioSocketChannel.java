package com.example.nionetty.channel.nio;

import com.example.nionetty.channel.AbstractChannel;
import com.example.nionetty.channel.ChannelConfig;
import com.example.nionetty.channel.ChannelFuture;
import com.example.nionetty.channel.ChannelId;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * {@code NioSocketChannel} 클래스는 클라이언트 소켓 채널의 NIO 기반 구현체입니다.
 * 이 클래스는 클라이언트와의 연결 및 I/O 처리를 담당하며,
 * java.nio.channels.SocketChannel을 내부적으로 사용합니다.
 */
public class NioSocketChannel extends AbstractChannel {

    private static final Logger logger = LoggerFactory.getLogger(NioSocketChannel.class);

    private SocketChannel javaChannel;

    public NioSocketChannel(ChannelId id, ChannelConfig config) {
        super(id, config);
    }

    public NioSocketChannel(ChannelId id, ChannelConfig config, SocketChannel javaChannel) {
        super(id, config);
        this.javaChannel = javaChannel;
    }

    public SocketChannel getJavaChannel() {
        return javaChannel;
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress) {
        ChannelFuture future = new ChannelFuture();
        try {
            logger.info("클라이언트 소켓 채널을 {}에 바인딩 시도", localAddress);
            // SocketChannel 생성 및 비블로킹 모드 설정
            javaChannel = SocketChannel.open();
            javaChannel.configureBlocking(false);
            javaChannel.bind(localAddress);
            future.setSuccess().setChannel(this);
            logger.info("클라이언트 소켓 채널 바인딩 성공: {}", localAddress);
        } catch (IOException e) {
            logger.error("클라이언트 소켓 채널 바인딩 실패", e);
            future.setFailure(e);
        }
        return future;
    }

    @Override
    public ChannelFuture close() {
        ChannelFuture future = new ChannelFuture();
        try {
            if (javaChannel != null) {
                javaChannel.close();
            }
            closeFuture.setSuccess();
            future.setSuccess();
            logger.info("클라이언트 소켓 채널 종료 성공");
        } catch (IOException e) {
            logger.error("클라이언트 소켓 채널 종료 실패", e);
            future.setFailure(e);
        }
        return future;
    }

    @Override
    public ChannelFuture write(Object msg) {
        // 간단한 임시 구현: 메시지를 문자열로 변환하여 콘솔에 출력
        logger.debug("NioSocketChannel writing message: {}", msg);
        // 실제 구현에서는 javaChannel.write(ByteBuffer.wrap(...)) 등을 사용
        return new ChannelFuture().setSuccess().setChannel(this);
    }
}
