package com.sdu.zrpc.framework.core.handler.inbound;

import com.sdu.zrpc.framework.common.entity.rpc.RpcRequest;
import com.sdu.zrpc.framework.common.entity.rpc.RpcRequestHolder;
import com.sdu.zrpc.framework.common.entity.rpc.RpcResponse;
import com.sdu.zrpc.framework.common.enums.RespCode;
import com.sdu.zrpc.framework.common.exception.ResponseException;
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
        Channel channel = channelHandlerContext.channel();
        SocketAddress socketAddress = channel.remoteAddress();
        Map<SocketAddress, Breaker> ipBreaker = RpcBootstrap.getInstance().getConfiguration().getIpBreaker();
        Breaker breaker = ipBreaker.get(socketAddress);

        Byte code = response.getCode();
        if (code == RespCode.HEARTBEAT.getCode()) {
            completableFuture.complete(null);
            RpcBootstrap.PENDING_REQUEST.remove(response.getRequestId());
            log.info("Heartbeat signal");
        } else if (code == RespCode.SUCCESS.getCode()) {
            Object responseBody = response.getBody();
            completableFuture.complete(responseBody);
            log.info("Id为【{}】的处理响应结果。", response.getRequestId());
        } else if (code == RespCode.FAIL.getCode()) {
            breaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为[{}]的请求，返回错误的结果，响应码[{}].", response.getRequestId(), response.getCode());
            throw new ResponseException(code, RespCode.FAIL.getDesc());
        } else if (code == RespCode.CLOSING.getCode()) {
            completableFuture.complete(null);
            log.error("当前id为[{}]的请求，访问被拒绝，目标服务器正处于关闭中，响应码[{}].", response.getRequestId(), response.getCode());
            // 将Channel从健康列表移除
            RpcBootstrap.CHANNEL_CACHE.remove((InetSocketAddress) socketAddress);
            RpcRequest request = RpcRequestHolder.get();
            // 重新进行负载均衡
            RpcBootstrap.getInstance().getLoadBalanceType().reload(request.getRequestPayload().getPath(), RpcBootstrap.CHANNEL_CACHE.keySet().stream().toList());
            throw new ResponseException(code, RespCode.CLOSING.getDesc());
        } else if (code == RespCode.RATE_LIMIT.getCode()) {
            breaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为[{}]的请求，被限流，响应码[{}].", response.getRequestId(), response.getCode());
            throw new ResponseException(code, RespCode.RATE_LIMIT.getDesc());
        } else if (code == RespCode.RESOURCE_NOT_FOUND.getCode()) {
            breaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为[{}]的请求，未找到目标资源，响应码[{}].", response.getRequestId(), response.getCode());
            throw new ResponseException(code, RespCode.RESOURCE_NOT_FOUND.getDesc());
        }
        // 将请求从提交列表移除
        RpcBootstrap.PENDING_REQUEST.remove(response.getRequestId());
    }
}
