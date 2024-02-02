package com.sdu.zrpc.framework.core.handler.inbound;

import com.sdu.zrpc.framework.common.entity.rpc.RpcRequest;
import com.sdu.zrpc.framework.common.entity.rpc.RpcRequestHolder;
import com.sdu.zrpc.framework.common.entity.rpc.RpcResponse;
import com.sdu.zrpc.framework.common.enums.RespCode;
import com.sdu.zrpc.framework.common.exception.ResponseException;
import com.sdu.zrpc.framework.common.response.ResponseResult;
import com.sdu.zrpc.framework.core.config.RpcBootstrap;
import com.sdu.zrpc.framework.core.protection.Breaker;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class SimpleChannelHandler extends SimpleChannelInboundHandler<RpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse response) {
        CompletableFuture<Object> completableFuture = RpcBootstrap.PENDING_REQUEST.get(response.getRequestId());
        if (completableFuture.isDone()) return;
        Channel channel = channelHandlerContext.channel();
        SocketAddress socketAddress = channel.remoteAddress();
        Map<SocketAddress, Breaker> ipBreaker = RpcBootstrap.getInstance().getConfiguration().getIpBreaker();
        Breaker breaker = ipBreaker.get(socketAddress);

        Byte responseCode = response.getCode();
        Long requestId = response.getRequestId();
        // 将请求从提交列表移除
        RpcBootstrap.PENDING_REQUEST.remove(requestId);
        if (responseCode == RespCode.HEARTBEAT.getCode()) {
            completableFuture.complete(null);
            log.info("HeartBeat");
        } else if (responseCode == RespCode.SUCCESS.getCode()) {
            Object responseBody = response.getBody();
            completableFuture.complete(ResponseResult.ok(responseBody));
            log.info("Id为【{}】的处理响应结果。", requestId);
        } else if (responseCode == RespCode.FAIL.getCode()) {
            breaker.recordErrorRequest();
            completableFuture.complete(ResponseResult.invokeError(requestId, responseCode));
            log.error("当前id为【{}】的请求，返回错误的结果，响应码【{}】.", requestId, responseCode);
            throw new ResponseException(responseCode, RespCode.FAIL.getDesc());
        } else if (responseCode == RespCode.CLOSING.getCode()) {
            completableFuture.complete(ResponseResult.invokeRejected(requestId, responseCode));
            log.error("当前id为【{}】的请求访问被拒绝，目标服务器正处于关闭中，响应码【{}】", requestId, responseCode);
            // 将Channel从健康列表移除
            RpcBootstrap.CHANNEL_CACHE.remove((InetSocketAddress) socketAddress);
            RpcRequest request = RpcRequestHolder.get();
            // 重新进行负载均衡
            RpcBootstrap.getInstance().getLoadBalanceType().reload(request.getRequestPayload().getPath(), RpcBootstrap.CHANNEL_CACHE.keySet().stream().toList());
            throw new ResponseException(responseCode, RespCode.CLOSING.getDesc());
        } else if (responseCode == RespCode.RATE_LIMIT.getCode()) {
            breaker.recordErrorRequest();
            completableFuture.complete(ResponseResult.invokeLimited(requestId, responseCode));
            log.error("当前id为【{}】的请求被限流，响应码【{}】", requestId, responseCode);
            throw new ResponseException(responseCode, RespCode.RATE_LIMIT.getDesc());
        } else if (responseCode == RespCode.RESOURCE_NOT_FOUND.getCode()) {
            breaker.recordErrorRequest();
            completableFuture.complete(ResponseResult.invokeResourceNotFound(requestId, responseCode));
            log.error("当前id为【{}】的请求未找到目标资源，响应码【{}】", requestId, responseCode);
            throw new ResponseException(responseCode, RespCode.RESOURCE_NOT_FOUND.getDesc());
        }
    }
}
