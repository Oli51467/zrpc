package com.sdu.provider;

import com.sdu.irpc.framework.core.config.IRpcBootstrap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class RpcStarter implements CommandLineRunner {

    @Autowired
    private Environment environment;

    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(1000);

        log.info("Rpc framework start!");
        IRpcBootstrap.getInstance().getConfiguration().setPort(Integer.parseInt(Objects.requireNonNull(environment.getProperty("local.server.port"))));
        IRpcBootstrap.getInstance().scanServices("com.sdu.provider.impl").start();
    }
}
