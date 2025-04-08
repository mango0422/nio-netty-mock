package com.example.nionetty.channel;

/**
 * {@code ChannelInitializer}는 새로 생성된 {@link Channel}의 초기 설정을 담당하기 위한 추상 클래스입니다.
 * <p>
 * 서브 클래스는 {@link #initChannel(Channel)} 메서드를 구현하여 해당 채널의 파이프라인 구성과 기타 초기화를 수행합니다.
 * 초기화가 완료되면 {@code ChannelInitializer}는 파이프라인에서 자신을 제거하여 이후 이벤트 전달에 방해가 없도록 합니다.
 * </p>
 *
 * @param <T> 초기화할 채널 타입, {@link Channel}의 하위 타입
 * 
 * @see ChannelInboundHandlerAdapter
 * @see ChannelHandlerContext
 * 
 * @author 
 * @version 1.0
 */
public abstract class ChannelInitializer<T extends Channel> extends ChannelInboundHandlerAdapter {

    /**
     * 채널이 파이프라인에 추가될 때 호출되며, 채널 초기화를 위한 {@link #initChannel(Channel)}을 수행합니다.
     * 초기화 후 자신을 파이프라인에서 제거합니다.
     *
     * @param ctx 채널 핸들러 컨텍스트
     * @throws Exception 초기화 과정에서 발생 가능한 예외
     */
    @Override
    public final void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        try {
            @SuppressWarnings("unchecked")
            T channel = (T) ctx.channel();
            initChannel(channel);
        } finally {
            // 초기화 후, 현재 핸들러를 파이프라인에서 제거하여 이후 이벤트 전달에 방해되지 않도록 합니다.
            ctx.pipeline().remove(this);
        }
    }

    /**
     * 채널 초기화를 위한 추상 메서드.
     * 서브 클래스는 이 메서드를 구현하여 채널 파이프라인 구성 및 초기 설정을 수행해야 합니다.
     *
     * @param channel 초기화할 채널
     * @throws Exception 초기화 과정에서 발생 가능한 예외
     */
    protected abstract void initChannel(T channel) throws Exception;
}
