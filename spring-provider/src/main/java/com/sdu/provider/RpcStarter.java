package com.sdu.provider;

import com.sdu.irpc.framework.core.config.IRpcBootstrap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RpcStarter implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(5000);
        log.info("Rpc framework start!");
        IRpcBootstrap.getInstance().scanServices("com.sdu.provider.impl").start();
    }
}
