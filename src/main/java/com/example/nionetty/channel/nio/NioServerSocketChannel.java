package com.example.nionetty.channel.nio;

import com.example.nionetty.channel.AbstractChannel;
import com.example.nionetty.channel.ChannelConfig;
import com.example.nionetty.channel.ChannelFuture;
import com.example.nionetty.channel.ChannelId;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.io.IOException;

/**
 * {@code NioServerSocketChannel} 클래스는 서버 소켓 채널의 NIO 기반 구현체입니다.
 * 실제 구현에서는 java.nio.channels.ServerSocketChannel을 내부적으로 사용하여
 * 소켓 바인딩 및 연결 수락을 수행합니다.
 */
public class NioServerSocketChannel extends AbstractChannel {

    private ServerSocketChannel javaChannel;

    public NioServerSocketChannel(ChannelId id, ChannelConfig config) {
        super(id, config);
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress) {
        ChannelFuture future = new ChannelFuture();
        try {
            // ServerSocketChannel 생성 및 비블로킹 모드 설정
            javaChannel = ServerSocketChannel.open();
            javaChannel.configureBlocking(false);
            javaChannel.socket().bind(localAddress);
            future.setSuccess().setChannel(this);
        } catch (IOException e) {
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
            // 채널 종료 후 closeFuture를 성공으로 처리
            closeFuture.setSuccess();
            future.setSuccess();
        } catch (IOException e) {
            future.setFailure(e);
        }
        return future;
    }

    @Override
    public ChannelFuture write(Object msg) {
        // 서버 소켓 채널에서는 일반적으로 write 연산이 없으므로, 단순 로그 출력
        System.out.println("NioServerSocketChannel write invoked with message: " + msg);
        return new ChannelFuture().setSuccess();
    }
}
