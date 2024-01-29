package com.sdu.zrpc.demo.provider.impl;

import com.sdu.zrpc.demo.api.Greet;
import com.sdu.zrpc.framework.common.annotation.ZrpcService;

@ZrpcService
public class GreetImpl implements Greet {

    @Override
    public String greet(String message) {
        return "Server echo greeting!";
    }
}