package com.sdu.arrow.demo.provider.impl;

import com.sdu.arrow.demo.api.Greet;
import com.sdu.arrow.framework.common.annotation.ArrowService;

@ArrowService
public class GreetImpl implements Greet {

    @Override
    public String greet(String message) {
        return "Server echo greeting!";
    }
}