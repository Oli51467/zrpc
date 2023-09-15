package com.sdu.sparrow.provider.impl;

import com.sdu.sparrow.framework.common.annotation.RpcMapping;
import com.sdu.sparrow.framework.common.annotation.RpcService;

@RpcService(application = "p1", path = "/greet")
public class GreetImpl {

    @RpcMapping(path = "/echo")
    public String greet(String message) {
        return "Server echo greeting!";
    }
}
