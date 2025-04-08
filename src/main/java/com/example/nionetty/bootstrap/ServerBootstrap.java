package com.example.nionetty.bootstrap;

import com.example.nionetty.channel.Channel;
import com.example.nionetty.channel.ChannelFuture;
import com.example.nionetty.channel.ChannelInitializer;
import com.example.nionetty.channel.DefaultChannelConfig;
import com.example.nionetty.channel.DefaultChannelId;
import com.example.nionetty.channel.nio.NioServerSocketChannel;
import com.example.nionetty.eventloop.EventLoopGroup;

import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;

/**
 * {@code ServerBootstrap} 클래스는 Netty 스타일 서버를 초기화하는
 * 부트스트랩 역할을 수행합니다.
 * <p>
 * 이 클래스는 이벤트 루프 그룹, 채널 클래스, 채널 초기화 핸들러 등을 설정하고,
 * 서버의 바인딩 및 실행을 관리합니다.
 * </p>
 */
public class ServerBootstrap {

    /** 클라이언트 연결 수락을 담당하는 boss 이벤트 루프 그룹 */
    private EventLoopGroup bossGroup;

    /** 클라이언트 I/O 처리를 담당하는 worker 이벤트 루프 그룹 */
    private EventLoopGroup workerGroup;

    /** 서버 채널의 구현 클래스 (예: {@code NioServerSocketChannel.class}) */
    private Class<?> channelClass;

    /** 신규 채널 초기화를 위한 핸들러 */
    private ChannelInitializer<?> channelInitializer;

    /**
     * 기본 생성자.
     */
    public ServerBootstrap() {
        // 내부 변수는 설정 메서드를 통해 초기화됨.
    }

    /**
     * 이벤트 루프 그룹을 설정합니다.
     *
     * @param bossGroup   클라이언트 연결 수락용 이벤트 루프 그룹
     * @param workerGroup 클라이언트 I/O 처리를 위한 이벤트 루프 그룹
     * @return 현재 {@code ServerBootstrap} 인스턴스 (빌더 패턴 지원)
     */
    public ServerBootstrap group(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        return this;
    }

    /**
     * 사용할 서버 채널 클래스를 설정합니다.
     *
     * @param channelClass 서버 채널의 클래스 (예: {@code NioServerSocketChannel.class})
     * @return 현재 {@code ServerBootstrap} 인스턴스
     */
    public ServerBootstrap channel(Class<?> channelClass) {
        this.channelClass = channelClass;
        return this;
    }

    /**
     * 채널 초기화 핸들러를 설정합니다.
     * <p>
     * 이 핸들러는 클라이언트와의 새로운 연결이 생성될 때마다 호출되어,
     * 해당 채널의 파이프라인 구성을 담당합니다.
     * </p>
     *
     * @param channelInitializer 채널 초기화를 위한 핸들러
     * @return 현재 {@code ServerBootstrap} 인스턴스
     */
    public ServerBootstrap childHandler(ChannelInitializer<?> channelInitializer) {
        this.channelInitializer = channelInitializer;
        return this;
    }

    /**
     * 지정한 포트로 서버를 바인딩하여 실행합니다.
     * <p>
     * 실제 구현에서는 bossGroup을 이용하여 서버 소켓 채널을 생성하고, 채널 초기화 핸들러
     * 를 이용해 새로 생성된 채널에 초기 설정을 적용한 후 workerGroup에 분배해야 합니다.
     * 여기는 최소 구현 예제로 채널 생성과 초기화, 그리고 바인딩만 처리합니다.
     * </p>
     *
     * @param port 서버가 바인딩할 포트 번호
     * @return 채널 바인딩 결과를 나타내는 {@code ChannelFuture} 객체
     */
    public ChannelFuture bind(int port) {
        ChannelFuture future;
        try {
            // channelClass가 Channel 인터페이스의 구현체(예: NioServerSocketChannel)라고 가정
            @SuppressWarnings("unchecked")
            Constructor<? extends Channel> constructor =
                    (Constructor<? extends Channel>) channelClass.getConstructor(
                            com.example.nionetty.channel.ChannelId.class,
                            com.example.nionetty.channel.ChannelConfig.class);
            // DefaultChannelId, DefaultChannelConfig를 이용해 채널 생성
            Channel channel = constructor.newInstance(new com.example.nionetty.channel.DefaultChannelId(),
                    new DefaultChannelConfig());

            // 채널 초기화 핸들러가 있다면 파이프라인에 추가 및 초기화
            if (channelInitializer != null) {
                channel.pipeline().addLast(channelInitializer);
                // ChannelInitializer 내부에서는 initChannel() 호출 후 자신을 제거하도록 구현되어 있음.
            }

            // 실제 채널 바인딩: InetSocketAddress로 포트 설정
            future = channel.bind(new InetSocketAddress(port));
        } catch (Exception e) {
            future = new ChannelFuture().setFailure(e);
        }
        return future;
    }
}
