package com.sdu.provider.impl;

import com.sdu.zrpc.api.GreetApi;
import com.sdu.zrpc.framework.common.annotation.ZrpcService;

@ZrpcService
public class GreetImpl implements GreetApi {

    @Override
    public String greet(String message) {
        return "Server echo greeting!";
    }

    @Override
    public String cal(int a, int b) {
        int c = a + b;
        return "Result: " + c;
    }
}
