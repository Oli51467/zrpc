package com.sdu.provider.impl;

import com.sdu.arrow.api.GreetApi;
import com.sdu.arrow.framework.common.annotation.ArrowService;

@ArrowService
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
