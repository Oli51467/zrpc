package com.sdu.arrow.provider.impl;

import com.sdu.arrow.framework.common.annotation.RpcMapping;
import com.sdu.arrow.framework.common.annotation.RpcService;

@RpcService(application = "p1", path = "/greet")
public class GreetImpl {

    @RpcMapping(path = "/echo")
    public String greet(String message) {
        return "Server echo greeting!";
    }
}
