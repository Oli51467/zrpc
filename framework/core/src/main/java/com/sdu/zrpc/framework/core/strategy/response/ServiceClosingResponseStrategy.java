package com.sdu.zrpc.framework.core.strategy.response;

import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson2.JSONObject;
import com.sdu.zrpc.framework.common.entity.rpc.RpcRequest;
import com.sdu.zrpc.framework.common.entity.rpc.RpcRequestHolder;
import com.sdu.zrpc.framework.common.entity.rpc.RpcResponse;
import com.sdu.zrpc.framework.common.enums.RespCode;
import com.sdu.zrpc.framework.common.exception.ResponseException;
import com.sdu.zrpc.framework.common.response.ResponseResult;
import com.sdu.zrpc.framework.core.config.RpcBootstrap;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ServiceClosingResponseStrategy extends AbstractRpcResponseStrategy implements RpcResponseStrategy {

    @Override
    public void complete(RpcResponse response, SocketAddress socketAddress) {
        recordError(socketAddress);
        Pair<CompletableFuture<Object>, Boolean> completableFutureResult = getCompletableFuture(response);
        if (!completableFutureResult.getValue()) return;
        CompletableFuture<Object> completableFuture = completableFutureResult.getKey();
        Byte responseCode = response.getCode();
        Long requestId = response.getRequestId();
        completableFuture.complete(JSONObject.toJSONString(ResponseResult.invokeRejected(requestId, responseCode)));
        // 将Channel从健康列表移除
        RpcBootstrap.CHANNEL_CACHE.remove((InetSocketAddress) socketAddress);
        RpcRequest request = RpcRequestHolder.get();
        // 重新进行负载均衡
        RpcBootstrap.getInstance().getLoadBalanceType().reload(request.getRequestPayload().getPath(), RpcBootstrap.CHANNEL_CACHE.keySet().stream().toList());
        log.error("当前id为【{}】的请求访问被拒绝，目标服务器正处于关闭中，响应码【{}】", requestId, responseCode);
        throw new ResponseException(responseCode, RespCode.CLOSING.getDesc());
    }
}
