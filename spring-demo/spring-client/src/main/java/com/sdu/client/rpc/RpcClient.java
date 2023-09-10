package com.sdu.client.rpc;

import com.sdu.irpc.framework.common.annotation.IrpcClient;
import com.sdu.irpc.framework.common.annotation.IrpcMapping;

@IrpcClient(application = "p1", path = "/test")
public interface RpcClient {

    @IrpcMapping(path = "/echo")
    String greet(String message);

    @IrpcMapping(path = "/cal")
    String cal(int a, int b);
}
