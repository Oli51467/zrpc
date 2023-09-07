package com.sdu.client;

import com.sdu.irpc.framework.common.annotation.IrpcClient;
import com.sdu.irpc.framework.common.annotation.IrpcMapping;

@IrpcClient(application = "p1", path = "greet.hello")
public interface RpcClient {

    @IrpcMapping(path = "/greet/echo")
    String greet(String message);
}
