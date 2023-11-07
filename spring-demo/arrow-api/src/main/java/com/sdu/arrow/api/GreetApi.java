package com.sdu.arrow.api;

import com.sdu.arrow.framework.common.annotation.RpcMapping;
import com.sdu.arrow.framework.common.annotation.RpcService;

@RpcService(application = "p1", path = "/test")
public interface GreetApi {
    @RpcMapping(path = "/echo")
    String greet(String message);

    @RpcMapping(path = "/cal")
    String cal(int a, int b);
}
