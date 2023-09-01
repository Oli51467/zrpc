package com.sdu.irpc.framework.core.handler.inbound;

import com.sdu.irpc.framework.common.enums.RespCode;
import com.sdu.irpc.framework.common.exception.ResponseException;
import com.sdu.irpc.framework.core.IRpcBootstrap;
import com.sdu.irpc.framework.common.entity.rpc.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class SimpleChannelHandler extends SimpleChannelInboundHandler<RpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse response) {
        CompletableFuture<Object> completableFuture = IRpcBootstrap.PENDING_REQUEST.get(response.getRequestId());

        Byte code = response.getCode();
        if (code == RespCode.SUCCESS.getCode()) {
            Object responseBody = response.getBody();
            completableFuture.complete(responseBody);
            log.info("Id为【{}】的处理响应结果。", response.getRequestId());
            IRpcBootstrap.PENDING_REQUEST.remove(response.getRequestId());
        } else if (code == RespCode.FAIL.getCode()) {
            completableFuture.complete(null);
            log.error("当前id为[{}]的请求，返回错误的结果，响应码[{}].", response.getRequestId(),response.getCode());
            throw new ResponseException(code,RespCode.FAIL.getDesc());
        } else if (code == RespCode.SUCCESS_HEART_BEAT.getCode()) {
            completableFuture.complete(null);
            IRpcBootstrap.PENDING_REQUEST.remove(response.getRequestId());
        }
    }
}
