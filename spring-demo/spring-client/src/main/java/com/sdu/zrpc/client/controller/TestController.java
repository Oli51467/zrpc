package com.sdu.zrpc.client.controller;

import com.alibaba.fastjson2.JSONObject;
import com.sdu.zrpc.api.GreetApi;
import com.sdu.zrpc.framework.common.annotation.ZrpcReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class TestController {

    @ZrpcReference(waitTime = 100, waitTimeUnit = TimeUnit.MILLISECONDS)
    public GreetApi client;

    @RequestMapping(value = "/echo", method = RequestMethod.GET)
    public Object greet() {
        String result = client.greet("Client say hi");
        return JSONObject.parseObject(result);
    }

    @RequestMapping(value = "/cal", method = RequestMethod.GET)
    public String greet1() {
        return client.cal(5, 5);
    }
}
