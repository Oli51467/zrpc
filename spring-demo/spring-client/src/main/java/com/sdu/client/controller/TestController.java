package com.sdu.client.controller;

import com.sdu.client.Greet;
import com.sdu.irpc.framework.common.annotation.IrpcProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TestController {

    @IrpcProxy
    public Greet greet;

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String greet() {
        return greet.greet("Client say hi");
    }
}
