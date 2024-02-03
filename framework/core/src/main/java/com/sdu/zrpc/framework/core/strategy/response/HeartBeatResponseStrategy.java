package com.sdu.zrpc.framework.core.strategy.response;

import cn.hutool.core.lang.Pair;
import com.sdu.zrpc.framework.common.entity.rpc.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class HeartBeatResponseStrategy extends AbstractRpcResponseStrategy implements RpcResponseStrategy {

    @Override
    public void complete(RpcResponse response, SocketAddress socketAddress) {
        recordSuccess(socketAddress);
        Pair<CompletableFuture<Object>, Boolean> completableFuture = getCompletableFuture(response);
        if (!completableFuture.getValue()) return;
        completableFuture.getKey().complete(null);
        log.info("HeartBeat");
    }
}
