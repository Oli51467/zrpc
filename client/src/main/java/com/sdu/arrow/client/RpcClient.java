package com.sdu.arrow.client;

import com.sdu.arrow.framework.common.annotation.RpcMapping;

@com.sdu.arrow.framework.common.annotation.RpcClient(application = "p1", path = "greet.hello")
public interface RpcClient {

    @RpcMapping(path = "/greet/echo")
    String greet(String message);
}
