package com.sdu.arrow.client;

import com.sdu.arrow.framework.common.annotation.RpcMapping;
import com.sdu.arrow.framework.common.annotation.RpcService;

@RpcService(application = "p1", path = "greet.hello")
public interface RpcClient {

    @RpcMapping(path = "/greet/echo")
    String greet(String message);
}
