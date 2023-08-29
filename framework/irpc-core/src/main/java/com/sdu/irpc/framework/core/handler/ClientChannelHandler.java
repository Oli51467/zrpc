package com.sdu.irpc.framework.core.handler;

import com.sdu.irpc.framework.core.IRpcBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

public class ClientChannelHandler extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        socketChannel.pipeline()
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                .addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf o) throws Exception {
                        String result = o.toString(Charset.defaultCharset());
                        CompletableFuture<Object> completableFuture = IRpcBootstrap.PENDING_REQUEST.get(1L);
                        completableFuture.complete(result);
                    }
                });
    }
}
