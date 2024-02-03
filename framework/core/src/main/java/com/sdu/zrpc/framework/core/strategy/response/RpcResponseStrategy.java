package com.sdu.zrpc.framework.core.strategy.response;

import com.sdu.zrpc.framework.common.entity.rpc.RpcResponse;

import java.net.SocketAddress;

public interface RpcResponseStrategy {

    void complete(RpcResponse response, SocketAddress socketAddress);
}
