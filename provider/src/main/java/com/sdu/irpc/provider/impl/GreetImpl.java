package com.sdu.irpc.provider.impl;

import com.sdu.irpc.api.Greet;

public class GreetImpl implements Greet {

    @Override
    public String greet(String message) {
        return "Server echo greeting!";
    }
}
