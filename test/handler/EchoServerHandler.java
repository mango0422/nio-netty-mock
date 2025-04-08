package com.example.nionetty.handler;

import com.example.nionetty.buffer.CustomBuffer; // Using our custom buffer
import com.example.nionetty.channel.ChannelHandlerContext;
import com.example.nionetty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * Handles a server-side channel by logging incoming messages and echoing them back.
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(EchoServerHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
         log.info("Channel active: {}", ctx.channel().remoteAddress());
         super.channelActive(ctx); // Forward event
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel inactive: {}", ctx.channel().remoteAddress());
        super.channelInactive(ctx); // Forward event
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof CustomBuffer) {
            CustomBuffer buffer = (CustomBuffer) msg;
            // Log received message (assuming UTF-8 text for logging)
            // Creating string consumes the buffer's readable bytes in this simple impl
            String receivedText = buffer.toString(StandardCharsets.UTF_8);
             log.info("Received from [{}]: {}", ctx.channel().remoteAddress(), receivedText);

            // Echo back the received data.
            // IMPORTANT: In real Netty, you need to handle buffer ownership (Reference Counting).
            // Here we assume the buffer read previously can be directly written back.
            // If toString() consumed the buffer, we might need to wrap the original bytes again.
            // Let's assume CustomBuffer.toString() didn't consume for simplicity, or re-wrap.

             // Re-wrap the text to send back (safer if toString consumed)
             CustomBuffer outBuffer = CustomBuffer.wrap(receivedText.getBytes(StandardCharsets.UTF_8));

             log.info("Echoing back to [{}]: {}", ctx.channel().remoteAddress(), receivedText);
             ctx.writeAndFlush(outBuffer); // Write and flush immediately


             // If using reference counting:
             // ByteBuf in = (ByteBuf) msg;
             // try { log(in); ctx.writeAndFlush(in.retainedDuplicate()); } finally { in.release(); }

        } else {
            // Pass along if not the type we handle
             log.warn("Received unexpected message type: {}", msg.getClass().getName());
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.trace("Channel read complete: {}", ctx.channel().remoteAddress());
        // Flushing could be done here instead of in channelRead if batching writes
        // ctx.flush();
        super.channelReadComplete(ctx); // Forward event
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // Log the exception and close the connection
        log.error("Exception caught in handler for channel {}: {}", ctx.channel().remoteAddress(), cause.getMessage(), cause);
        ctx.close(); // Close the connection on error
    }
}