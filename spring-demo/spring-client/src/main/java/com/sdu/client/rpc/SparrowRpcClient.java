package com.sdu.client.rpc;

import com.sdu.sparrow.framework.common.annotation.RpcClient;
import com.sdu.sparrow.framework.common.annotation.RpcMapping;

@RpcClient(application = "p1", path = "/test")
public interface SparrowRpcClient {

    @RpcMapping(path = "/echo")
    String greet(String message);

    @RpcMapping(path = "/cal")
    String cal(int a, int b);
}
