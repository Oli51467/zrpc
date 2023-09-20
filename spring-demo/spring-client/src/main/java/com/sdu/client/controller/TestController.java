package com.sdu.client.controller;

import com.sdu.client.rpc.ArrowRpcClient;
import com.sdu.arrow.framework.common.annotation.FrequencyControl;
import com.sdu.arrow.framework.common.annotation.RpcProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TestController {

    @RpcProxy
    public ArrowRpcClient client;

    @RequestMapping(value = "/echo", method = RequestMethod.GET)
    @FrequencyControl(time = 3, count = 1, target = FrequencyControl.Target.IP)
    public String greet() {
        return client.greet("Client say hi");
    }

    @RequestMapping(value = "/cal", method = RequestMethod.GET)
    public String greet1() {
        return client.cal(5, 5);
    }
}
