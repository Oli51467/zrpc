package com.sdu.zrpc.framework.core.handler.inbound;

import com.sdu.zrpc.framework.common.entity.rpc.RpcResponse;
import com.sdu.zrpc.framework.core.strategy.response.RpcResponseStrategyContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

@Slf4j
public class SimpleChannelHandler extends SimpleChannelInboundHandler<RpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse response) {
        SocketAddress socketAddress = channelHandlerContext.channel().remoteAddress();
        RpcResponseStrategyContext.getResponseStrategy(response.getCode()).complete(response, socketAddress);
    }
}
