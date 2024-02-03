package com.sdu.zrpc.framework.core.strategy.response;

import cn.hutool.core.lang.Pair;
import com.sdu.zrpc.framework.common.entity.rpc.RpcResponse;
import com.sdu.zrpc.framework.core.config.RpcBootstrap;
import com.sdu.zrpc.framework.core.protection.Breaker;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractRpcResponseStrategy implements RpcResponseStrategy {

    public Pair<CompletableFuture<Object>, Boolean> getCompletableFuture(RpcResponse response) {
        // 获取正在阻塞等待结果的CompletableFuture
        CompletableFuture<Object> completableFuture = RpcBootstrap.PENDING_REQUEST.get(response.getRequestId());
        if (null == completableFuture) return new Pair<>(null, false);
        // 将请求从提交列表移除
        RpcBootstrap.PENDING_REQUEST.remove(response.getRequestId());
        if (completableFuture.isDone()) return new Pair<>(null, false);
        return new Pair<>(completableFuture, true);
    }

    public void recordSuccess(SocketAddress socketAddress) {
        Map<SocketAddress, Breaker> ipBreaker = RpcBootstrap.getInstance().getConfiguration().getIpBreaker();
        Breaker breaker = ipBreaker.get(socketAddress);
        if (null != breaker) breaker.recordSuccessRequest();
    }

    public void recordError(SocketAddress socketAddress) {
        Map<SocketAddress, Breaker> ipBreaker = RpcBootstrap.getInstance().getConfiguration().getIpBreaker();
        Breaker breaker = ipBreaker.get(socketAddress);
        if (null != breaker) breaker.recordErrorRequest();
    }

    public abstract void complete(RpcResponse response, SocketAddress socketAddress);
}
