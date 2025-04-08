package com.example.nionetty.bootstrap;

import com.example.nionetty.channel.Channel;
import com.example.nionetty.channel.ChannelFuture;
import com.example.nionetty.channel.ChannelInitializer;
import com.example.nionetty.channel.nio.NioServerSocketChannel;
import com.example.nionetty.eventloop.EventLoopGroup;
import com.example.nionetty.channel.nio.NioEventLoopGroup;
import com.example.nionetty.handler.EchoServerHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code ServerBootstrapRunner} 클래스는 애플리케이션의 진입점(main 메서드)이며,
 * {@code ServerBootstrap}을 이용하여 서버를 초기화하고 실행합니다.
 */
public class ServerBootstrapRunner {

    private static final Logger logger = LoggerFactory.getLogger(ServerBootstrapRunner.class);

    /**
     * 애플리케이션의 메인 메서드.
     *
     * @param args 커맨드 라인 인자들
     */
    public static void main(String[] args) {
        logger.info("서버 부트스트랩 시작...");
        try {
            // bossGroup: 클라이언트 연결 수락 전용 이벤트 루프 그룹 (단일 스레드 사용)
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            // workerGroup: 클라이언트 I/O 처리를 위한 이벤트 루프 그룹 (기본 스레드 수)
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            // 서버 부트스트랩 구성 및 실행
            ChannelFuture future = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        /**
                         * 각 클라이언트 채널 초기화 시 호출되어 파이프라인을 구성합니다.
                         *
                         * @param ch 초기화할 채널
                         * @throws Exception 초기화 중 발생 가능한 예외
                         */
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            // 채널 파이프라인에 예제 에코 핸들러를 추가합니다.
                            ch.pipeline().addLast(new EchoServerHandler());
                        }
                    })
                    .bind(8080); // 8080 포트에 바인딩

            // 바인딩 완료 후 채널 객체를 받아오기
            Channel channel = future.sync().channel();
            logger.info("서버가 포트 8080에 성공적으로 바인딩되었습니다.");

            // 채널 종료가 발생할 때까지 메인 스레드 블로킹
            channel.closeFuture().sync();

            logger.info("서버가 포트 8080에 성공적으로 바인딩되었습니다.");
        } catch (Exception e) {
            logger.error("서버 부트스트랩 중 오류 발생", e);
            Thread.currentThread().interrupt();
        }
    }
}
