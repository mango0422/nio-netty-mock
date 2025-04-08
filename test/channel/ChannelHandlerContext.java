// package com.example.nionetty.channel;

// import com.example.nionetty.util.AttributeKey; // For attributes
// import com.example.nionetty.util.AttributeMap; // For attributes
// import com.example.nionetty.util.concurrent.EventExecutor; // Base for EventLoop

// import java.net.SocketAddress;

// /**
//  * Enables a {@link ChannelHandler} to interact with its {@link ChannelPipeline}
//  * and other handlers. Among other things a handler can notify the next {@link ChannelHandler} in the
//  * {@link ChannelPipeline} as well as modify the {@link ChannelPipeline} it belongs to dynamically.
//  */
// public interface ChannelHandlerContext extends AttributeMap { // Can hold attributes

//     /**
//      * Return the {@link Channel} which is bound to the {@link ChannelHandlerContext}.
//      */
//     Channel channel();

//     /**
//      * Returns the {@link EventExecutor} which is used to execute tasks for the {@link Channel}.
//      */
//     EventExecutor executor(); // Usually the EventLoop

//     /**
//      * Returns the unique name of the {@link ChannelHandlerContext}.The name was used when then
//      * {@link ChannelHandler} was added to the {@link ChannelPipeline}. This name can also be used to access the
//      * handler again like {@link ChannelPipeline#context(String)}.
//      */
//     String name();

//     /**
//      * Returns the {@link ChannelHandler} which belongs to the {@link ChannelHandlerContext}.
//      */
//     ChannelHandler handler();

//     /**
//      * Return {@code true} if the {@link ChannelHandler} which belongs to the {@link ChannelHandlerContext}
//      * was removed from the {@link ChannelPipeline}.
//      */
//     boolean isRemoved();

//     /**
//      * Return the {@link ChannelPipeline} which belongs to the {@link ChannelHandlerContext}.
//      */
//     ChannelPipeline pipeline();

//     // --- Event Propagation Methods (Inbound) ---

//     /**
//      * A {@link Channel} was registered to its {@link EventLoop}.
//      *
//      * This will result in having the {@link ChannelInboundHandler#channelRegistered(ChannelHandlerContext)} method
//      * called of the next {@link ChannelInboundHandler} contained in the {@link ChannelPipeline}.
//      */
//     ChannelHandlerContext fireChannelRegistered();

//     /**
//      * A {@link Channel} was unregistered from its {@link EventLoop}.
//      *
//      * This will result in having the {@link ChannelInboundHandler#channelUnregistered(ChannelHandlerContext)} method
//      * called of the next {@link ChannelInboundHandler} contained in the {@link ChannelPipeline}.
//      */
//     ChannelHandlerContext fireChannelUnregistered();


//     /**
//      * A {@link Channel} is active now, which means it is connected.
//      *
//      * This will result in having the {@link ChannelInboundHandler#channelActive(ChannelHandlerContext)} method
//      * called of the next {@link ChannelInboundHandler} contained in the {@link ChannelPipeline}.
//      */
//     ChannelHandlerContext fireChannelActive();

//     /**
//      * A {@link Channel} is inactive now, which means it is closed.
//      *
//      * This will result in having the {@link ChannelInboundHandler#channelInactive(ChannelHandlerContext)} method
//      * called of the next {@link ChannelInboundHandler} contained in the {@link ChannelPipeline}.
//      */
//     ChannelHandlerContext fireChannelInactive();

//     /**
//      * A {@link Channel} received a message.
//      *
//      * This will result in having the {@link ChannelInboundHandler#channelRead(ChannelHandlerContext, Object)} method
//      * called of the next {@link ChannelInboundHandler} contained in the {@link ChannelPipeline}.
//      */
//     ChannelHandlerContext fireChannelRead(Object msg);

//     /**
//      * Triggers an {@link ChannelInboundHandler#channelReadComplete(ChannelHandlerContext)}
//      * event to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
//      */
//     ChannelHandlerContext fireChannelReadComplete();

//     /**
//      * Triggers an {@link ChannelInboundHandler#channelWritabilityChanged(ChannelHandlerContext)}
//      * event to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
//      */
//     ChannelHandlerContext fireChannelWritabilityChanged();


//     /**
//      * Triggers an exception event to the next {@link ChannelHandler} in the {@link ChannelPipeline}.
//      */
//     ChannelHandlerContext fireExceptionCaught(Throwable cause);

//     /**
//      * Triggers an user event to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
//      */
//     ChannelHandlerContext fireUserEventTriggered(Object evt);


//     // --- Operation Methods (Outbound) ---

//     /**
//      * Request to bind to the given {@link SocketAddress} and notify the {@link ChannelFuture}.
//      * This will result in having the
//      * {@link ChannelOutboundHandler#bind(ChannelHandlerContext, SocketAddress, ChannelPromise)} method
//      * called of the next {@link ChannelOutboundHandler} contained in the {@link ChannelPipeline}.
//      */
//     ChannelFuture bind(SocketAddress localAddress);
//     ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise);


//     /**
//      * Request to connect to the given {@link SocketAddress} and notify the {@link ChannelFuture}.
//      * This will result in having the
//      * {@link ChannelOutboundHandler#connect(ChannelHandlerContext, SocketAddress, SocketAddress, ChannelPromise)} method
//      * called of the next {@link ChannelOutboundHandler} contained in the {@link ChannelPipeline}.
//      */
//     ChannelFuture connect(SocketAddress remoteAddress);
//     ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress);
//     ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise);
//     ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise);

//     /**
//      * Request to disconnect from the remote peer and notify the {@link ChannelFuture}.
//      * This will result in having the
//      * {@link ChannelOutboundHandler#disconnect(ChannelHandlerContext, ChannelPromise)} method
//      * called of the next {@link ChannelOutboundHandler} contained in the {@link ChannelPipeline}.
//      */
//     ChannelFuture disconnect();
//     ChannelFuture disconnect(ChannelPromise promise);


//     /**
//      * Request to close the {@link Channel} and notify the {@link ChannelFuture}.
//      * This will result in having the
//      * {@link ChannelOutboundHandler#close(ChannelHandlerContext, ChannelPromise)} method
//      * called of the next {@link ChannelOutboundHandler} contained in the {@link ChannelPipeline}.
//      */
//     ChannelFuture close();
//     ChannelFuture close(ChannelPromise promise);

//     /**
//      * Request to deregister the {@link Channel} from its {@link EventLoop} and notify the {@link ChannelFuture}.
//      * This will result in having the
//      * {@link ChannelOutboundHandler#deregister(ChannelHandlerContext, ChannelPromise)} method
//      * called of the next {@link ChannelOutboundHandler} contained in the {@link ChannelPipeline}.
//      * (Note: Deregister is often less common in outbound path, might be missing in some models)
//      */
//     // ChannelFuture deregister();
//     // ChannelFuture deregister(ChannelPromise promise);


//     /**
//      * Request to read data from the {@link Channel}. This will result in having the
//      * {@link ChannelOutboundHandler#read(ChannelHandlerContext)} method called of the next
//      * {@link ChannelOutboundHandler} contained in the {@link ChannelPipeline}.
//      */
//     ChannelHandlerContext read();

//     /**
//      * Request to write a message via this {@link ChannelHandlerContext} through the {@link ChannelPipeline}.
//      * This will result in having the
//      * {@link ChannelOutboundHandler#write(ChannelHandlerContext, Object, ChannelPromise)} method
//      * called of the next {@link ChannelOutboundHandler} contained in the {@link ChannelPipeline}.
//      */
//     ChannelFuture write(Object msg);
//     ChannelFuture write(Object msg, ChannelPromise promise);

//     /**
//      * Request to flush pending data which was written via {@link #write(Object)}.
//      * This will result in having the
//      * {@link ChannelOutboundHandler#flush(ChannelHandlerContext)} method called of the next
//      * {@link ChannelOutboundHandler} contained in the {@link ChannelPipeline}.
//      */
//     ChannelHandlerContext flush();

//     /**
//      * Shortcut for calling {@link #write(Object, ChannelPromise)} and {@link #flush()}.
//      */
//     ChannelFuture writeAndFlush(Object msg, ChannelPromise promise);
//     ChannelFuture writeAndFlush(Object msg);

//     // Method from AttributeMap
//     @Override
//     <T> Attribute<T> attr(AttributeKey<T> key); // Needs Attribute and AttributeKey

//     @Override
//     <T> boolean hasAttr(AttributeKey<T> key);
// }

// // // Placeholders required:
// // package com.example.nionetty.channel;
// // import com.example.nionetty.util.*; // AttributeMap, Attribute, AttributeKey
// // import com.example.nionetty.util.concurrent.EventExecutor;
// // import java.net.SocketAddress;
// // // Channel, ChannelHandler, ChannelPipeline, ChannelFuture, ChannelPromise defined elsewhere
// // public interface ChannelOutboundHandler extends ChannelHandler { /* bind, connect, write, etc. */ }

package com.example.nionetty.channel;

import com.example.nionetty.util.Attribute;
import com.example.nionetty.util.AttributeKey;
import java.net.SocketAddress;

public interface ChannelHandlerContext {
    Channel channel();
    ChannelPipeline pipeline();
    EventLoop executor();
    String name();
    ChannelHandler handler();
    boolean isRemoved();

    // Inbound 이벤트 관련
    ChannelHandlerContext fireChannelRegistered();
    ChannelHandlerContext fireChannelUnregistered();
    ChannelHandlerContext fireChannelActive();
    ChannelHandlerContext fireChannelInactive();
    ChannelHandlerContext fireChannelRead(Object msg);
    ChannelHandlerContext fireChannelReadComplete();
    ChannelHandlerContext fireChannelWritabilityChanged();
    ChannelHandlerContext fireUserEventTriggered(Object evt);
    ChannelHandlerContext fireExceptionCaught(Throwable cause);

    // Outbound 연산 관련
    ChannelFuture bind(SocketAddress localAddress);
    ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise);
    ChannelFuture connect(SocketAddress remoteAddress);
    ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress);
    ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise);
    ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise);
    ChannelFuture disconnect();
    ChannelFuture disconnect(ChannelPromise promise);
    ChannelFuture close();
    ChannelFuture close(ChannelPromise promise);
    ChannelFuture deregister();
    ChannelFuture deregister(ChannelPromise promise);
    ChannelHandlerContext read();
    ChannelFuture write(Object msg);
    ChannelFuture write(Object msg, ChannelPromise promise);
    ChannelHandlerContext flush();
    ChannelFuture writeAndFlush(Object msg);
    ChannelFuture writeAndFlush(Object msg, ChannelPromise promise);

    // 속성 관련
    <T> Attribute<T> attr(AttributeKey<T> key);
    <T> boolean hasAttr(AttributeKey<T> key);
}
