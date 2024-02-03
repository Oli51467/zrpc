package com.sdu.zrpc.framework.core.strategy.response;

import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson2.JSONObject;
import com.sdu.zrpc.framework.common.entity.rpc.RpcResponse;
import com.sdu.zrpc.framework.common.response.ResponseResult;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class SuccessResponseStrategy extends AbstractRpcResponseStrategy implements RpcResponseStrategy{

    @Override
    public void complete(RpcResponse response, SocketAddress socketAddress) {
        recordSuccess(socketAddress);
        Pair<CompletableFuture<Object>, Boolean> completableFutureResult = getCompletableFuture(response);
        if (!completableFutureResult.getValue()) return;
        CompletableFuture<Object> completableFuture = completableFutureResult.getKey();
        Object responseBody = response.getBody();
        completableFuture.complete(JSONObject.toJSONString(ResponseResult.ok(responseBody)));
        log.info("Id为【{}】的处理响应结果调用成功。", response.getRequestId());
    }
}
