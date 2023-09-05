package com.sdu.irpc.provider.impl;

import com.sdu.irpc.framework.common.annotation.IrpcMapping;
import com.sdu.irpc.framework.common.annotation.IrpcService;

@IrpcService(application = "p1", path = "/greet")
public class GreetImpl {

    @IrpcMapping(path = "/echo")
    public String greet(String message) {
        return "Server echo greeting!";
    }
}
