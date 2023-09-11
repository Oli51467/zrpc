package com.sdu.provider;

import com.sdu.irpc.framework.common.annotation.EnableIrpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.sdu")
@EnableIrpc(basePackages = "com.sdu.provider.impl")
public class SpringProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringProviderApplication.class, args);
    }
}