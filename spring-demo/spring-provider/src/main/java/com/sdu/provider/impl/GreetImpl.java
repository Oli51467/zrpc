package com.sdu.provider.impl;

import com.sdu.sparrow.framework.common.annotation.RpcMapping;
import com.sdu.sparrow.framework.common.annotation.RpcService;

@RpcService(application = "p1", path = "/test")
public class GreetImpl {

    @RpcMapping(path = "/echo")
    public String greet(String message) {
        return "Server echo greeting!";
    }

    @RpcMapping(path = "/cal")
    public String cal(int a, int b) {
        int c = a + b;
        return "Result: " + c;
    }
}
