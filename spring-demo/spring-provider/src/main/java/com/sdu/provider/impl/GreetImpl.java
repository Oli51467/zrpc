package com.sdu.provider.impl;

import com.sdu.irpc.framework.common.annotation.IrpcMapping;
import com.sdu.irpc.framework.common.annotation.IrpcService;

@IrpcService(application = "p1", path = "/test")
public class GreetImpl {

    @IrpcMapping(path = "/echo")
    public String greet(String message) {
        return "Server echo greeting!";
    }

    @IrpcMapping(path = "/cal")
    public String greet1(int a, int b) {
        int c = a + b;
        return "Result: " + c;
    }
}
