package com.sdu.irpc.client;

import com.sdu.irpc.framework.common.annotation.IrpcClient;
import com.sdu.irpc.framework.common.annotation.IrpcMapping;

@IrpcClient(application = "p1")
public interface RpcClient {

    @IrpcMapping(path = "/greet/echo")
    String greet(String message);
}
