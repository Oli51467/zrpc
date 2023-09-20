package com.sdu.client.rpc;

import com.sdu.arrow.framework.common.annotation.RpcClient;
import com.sdu.arrow.framework.common.annotation.RpcMapping;

@RpcClient(application = "p1", path = "/test")
public interface ArrowRpcClient {

    @RpcMapping(path = "/echo")
    String greet(String message);

    @RpcMapping(path = "/cal")
    String cal(int a, int b);
}
