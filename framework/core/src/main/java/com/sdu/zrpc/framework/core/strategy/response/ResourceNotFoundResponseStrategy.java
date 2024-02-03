package com.sdu.zrpc.framework.core.strategy.response;

import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson2.JSONObject;
import com.sdu.zrpc.framework.common.entity.rpc.RpcResponse;
import com.sdu.zrpc.framework.common.enums.RespCode;
import com.sdu.zrpc.framework.common.exception.ResponseException;
import com.sdu.zrpc.framework.common.response.ResponseResult;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ResourceNotFoundResponseStrategy extends AbstractRpcResponseStrategy implements RpcResponseStrategy {

    @Override
    public void complete(RpcResponse response, SocketAddress socketAddress) {
        recordError(socketAddress);
        Pair<CompletableFuture<Object>, Boolean> completableFutureResult = getCompletableFuture(response);
        if (!completableFutureResult.getValue()) return;
        CompletableFuture<Object> completableFuture = completableFutureResult.getKey();
        Long requestId = response.getRequestId();
        Byte responseCode = response.getCode();
        completableFuture.complete(JSONObject.toJSONString(ResponseResult.invokeResourceNotFound(requestId, responseCode)));
        log.error("当前id为【{}】的请求未找到目标资源，响应码【{}】", requestId, responseCode);
        throw new ResponseException(responseCode, RespCode.RESOURCE_NOT_FOUND.getDesc());
    }
}
