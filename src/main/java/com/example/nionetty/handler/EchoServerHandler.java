package com.example.nionetty.handler;

import com.example.nionetty.channel.ChannelHandlerContext;
import com.example.nionetty.channel.ChannelInboundHandlerAdapter;

/**
 * {@code EchoServerHandler} 클래스는 클라이언트로부터 수신한 메시지를 그대로 반환하는 에코 서버 핸들러입니다.
 * 간단한 테스트용으로 사용되며, 실제 구현에서는 추가적인 로직을 포함할 수 있습니다.
 * 
 * (예제)
 * 
 * @author 
 * @version 1.0
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 받은 메시지를 그대로 기록(write)하여 에코 처리
        ctx.write(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // 모든 메시지 처리 후 플러시하여 전송 완료
        ctx.fireChannelReadComplete();
    }
}
