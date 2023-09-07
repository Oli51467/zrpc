package com.sdu.client.rpc;

import com.sdu.irpc.framework.common.annotation.IrpcClient;

@IrpcClient(application = "p1", path = "/greet/echo")
public interface RpcClient {

    String greet(String message);
}
