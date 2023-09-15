package com.sdu.sparrow.client;

import com.sdu.sparrow.framework.common.annotation.RpcMapping;

@com.sdu.sparrow.framework.common.annotation.RpcClient(application = "p1", path = "greet.hello")
public interface RpcClient {

    @RpcMapping(path = "/greet/echo")
    String greet(String message);
}
