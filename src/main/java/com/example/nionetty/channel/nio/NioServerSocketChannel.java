package com.example.nionetty.channel.nio;

import com.example.nionetty.channel.AbstractChannel;
import com.example.nionetty.channel.ChannelConfig;
import com.example.nionetty.channel.ChannelFuture;
import com.example.nionetty.channel.ChannelId;
import com.example.nionetty.channel.DefaultChannelConfig;
import com.example.nionetty.channel.DefaultChannelId;
import com.example.nionetty.eventloop.EventLoop;
import com.example.nionetty.eventloop.EventLoopGroup;

import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * {@code NioServerSocketChannel} 클래스는 서버 소켓 채널의 NIO 기반 구현체입니다.
 * 실제 구현에서는 java.nio.channels.ServerSocketChannel을 내부적으로 사용하여
 * 소켓 바인딩 및 연결 수락을 수행합니다.
 */
public class NioServerSocketChannel extends AbstractChannel {

    private static final Logger logger = LoggerFactory.getLogger(NioServerSocketChannel.class);

    private ServerSocketChannel javaChannel;
    private EventLoopGroup workerGroup;

    public NioServerSocketChannel(ChannelId id, ChannelConfig config) {
        super(id, config);
    }

    public void setWorkerGroup(EventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress) {
        ChannelFuture future = new ChannelFuture();
        try {
            // ServerSocketChannel 생성 및 비블로킹 모드 설정
            logger.info("서버 소켓을 {}에 바인딩 중...", localAddress);
            javaChannel = ServerSocketChannel.open();
            javaChannel.configureBlocking(false);
            javaChannel.socket().bind(localAddress);
            future.setSuccess().setChannel(this);
            logger.info("서버 소켓 바인딩 성공: {}", localAddress);

            // 바인딩 성공 후, 클라이언트 연결을 수락할 수 있도록
            // 별도의 스레드에서 acceptor 로직 실행
            new Thread(() -> {
                logger.info("Acceptor 스레드 시작");
                while (true) {
                    try {
                        // accept()는 non-blocking 모드이므로 연결이 없으면 null을 반환함
                        java.nio.channels.SocketChannel clientChannel = javaChannel.accept();
                        if (clientChannel != null) {
                            logger.info("Accepted connection from {}", clientChannel.getRemoteAddress());
                            // 수락된 클라이언트 채널을 NioSocketChannel로 감싸기
                            // TODO: 실제 구현에서는 bossGroup -> workerGroup으로 연결 전달 및 채널 초기화 진행
                            // 예제에서는 단순히 수락만 로그로 출력함.

                            // 만약 NioSocketChannel로 감싸고 싶다면, 새로운 NioSocketChannel 생성 시
                            // 해당 clientChannel을 활용하는 생성자를 정의하고 호출해야 합니다.
                            // 새 NioSocketChannel 생성 (수락된 clientChannel 사용)
                            NioSocketChannel ns = new NioSocketChannel(new DefaultChannelId(),
                                    new DefaultChannelConfig(), clientChannel);
                            // (원한다면 채널 초기화를 위한 childHandler를 호출할 수 있음)
                            // workerGroup에서 이벤트 루프를 얻어 등록
                            EventLoop worker = workerGroup.next();
                            logger.debug("Worker EventLoop 선택: {}", worker);
                            // Cast worker to NioEventLoop to use register() method
                            ((NioEventLoop) worker).register(ns);
                        } else {
                            // 연결이 없으면 잠시 대기 (예: 50ms)
                            Thread.sleep(50);
                        }
                    } catch (Exception ex) {
                        logger.error("Acceptor 스레드 오류", ex);
                    }
                }
            }, "Acceptor-Thread").start();
        } catch (IOException e) {
            logger.error("서버 소켓 바인딩 중 오류", e);
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
            logger.info("서버 소켓 채널 종료 완료");
        } catch (IOException e) {
            logger.error("서버 소켓 채널 종료 중 오류", e);
            future.setFailure(e);
        }
        return future;
    }

    @Override
    public ChannelFuture write(Object msg) {
        // 서버 소켓 채널에서는 일반적으로 write 연산이 없으므로, 단순 로그 출력
        logger.debug("NioServerSocketChannel write invoked with message: {}", msg);
        return new ChannelFuture().setSuccess();
    }
}
