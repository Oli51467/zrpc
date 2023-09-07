package com.sdu.client.rpc;

import com.sdu.irpc.framework.common.annotation.IrpcClient;

@IrpcClient(application = "p1", path = "/greet/cal")
public interface RpcClient1 {

    String greet1(int a, int b);
}
