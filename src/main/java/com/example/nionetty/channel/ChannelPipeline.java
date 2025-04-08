package com.example.nionetty.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * {@code ChannelPipeline} 클래스는 채널 핸들러들을 순차적으로 관리하며,
 * 발생하는 모든 이벤트를 핸들러 체인에 따라 전달합니다.
 * <p>
 * 핸들러들은 파이프라인에 등록된 순서대로 이벤트를 처리하며,
 * 각 핸들러는 {@link ChannelHandlerContext}를 통해 다음 핸들러로 이벤트를 전달할 수 있습니다.
 * </p>
 *
 * 예를 들어, 채널의 읽기, 쓰기, 예외 처리 등의 이벤트가 발생하면
 * 파이프라인 내의 각 핸들러가 순차적으로 호출되어 이벤트를 처리합니다.
 *
 * @see ChannelHandler
 * @see ChannelHandlerContext
 * 
 * @author
 * @version 1.0
 */
public class ChannelPipeline {

    /** 이 파이프라인에 등록된 핸들러들의 리스트 */
    private final List<ChannelHandler> handlers = new ArrayList<>();

    /** 소속된 채널 */
    private final Channel channel;

    /**
     * {@code ChannelPipeline} 생성자.
     *
     * @param channel 이 파이프라인이 소속될 {@link Channel}
     */
    public ChannelPipeline(Channel channel) {
        this.channel = channel;
    }

    /**
     * 파이프라인에 핸들러를 추가합니다.
     *
     * @param handler 추가할 {@link ChannelHandler} 객체
     * @return 현재 파이프라인 인스턴스 (빌더 패턴 지원)
     */
    public ChannelPipeline addLast(ChannelHandler handler) {
        handlers.add(handler);
        // 핸들러가 추가될 때 호출
        try {
            // 새로 추가된 핸들러에 handlerAdded 이벤트 전달
            ChannelHandlerContext ctx = new DefaultChannelHandlerContext(this, handler);
            handler.handlerAdded(ctx);
        } catch (Exception e) {
            // handlerAdded 호출 중 예외 발생 시 처리 (실제 구현에서는 로깅 처리)
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 파이프라인에서 지정된 핸들러를 제거합니다.
     *
     * @param handler 제거할 {@link ChannelHandler} 객체
     * @return 현재 파이프라인 인스턴스
     */
    public ChannelPipeline remove(ChannelHandler handler) {
        handlers.remove(handler);
        return this;
    }

    /**
     * 다음 핸들러에게 읽기 이벤트를 전달합니다.
     * <p>
     * 실제 구현에서는 현재 컨텍스트 다음의 핸들러를 찾아 {@code fireChannelRead()}를 호출해야 합니다.
     * 이 예제에서는 단순히 모든 핸들러에 순차적으로 이벤트를 전달합니다.
     * </p>
     *
     * @param msg 전달할 메시지 객체
     */
    public void fireChannelRead(Object msg) {
        for (ChannelHandler handler : handlers) {
            // 실제 사용 시, 각 핸들러에 대한 Context를 통한 호출 필요
            if (handler instanceof ChannelInboundHandler) {
                try {
                    ChannelHandlerContext ctx = new DefaultChannelHandlerContext(this, handler);
                    ((ChannelInboundHandler) handler).channelRead(ctx, msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 반환된 채널 객체를 제공합니다.
     *
     * @return 소속 {@link Channel} 객체
     */
    public Channel channel() {
        return channel;
    }

    /**
     * 파이프라인에 등록된 핸들러의 반복자를 반환합니다.
     *
     * @return {@link Iterator} 형태의 핸들러 반복자
     */
    public Iterator<ChannelHandler> iterator() {
        return handlers.iterator();
    }

    /**
     * 모든 inbound 핸들러에 대해 읽기 완료 이벤트를 전달합니다.
     */
    public void fireChannelReadComplete() {
        for (ChannelHandler handler : handlers) {
            if (handler instanceof ChannelInboundHandler) {
                try {
                    ChannelHandlerContext ctx = new DefaultChannelHandlerContext(this, handler);
                    ((ChannelInboundHandler) handler).channelReadComplete(ctx);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
