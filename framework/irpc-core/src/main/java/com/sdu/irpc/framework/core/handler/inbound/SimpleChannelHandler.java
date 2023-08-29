package com.sdu.irpc.framework.core.handler.inbound;

import com.sdu.irpc.framework.core.IRpcBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

public class SimpleChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf o) {
        String result = o.toString(Charset.defaultCharset());
        CompletableFuture<Object> completableFuture = IRpcBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(result);
        IRpcBootstrap.PENDING_REQUEST.remove(1L);
    }
}
