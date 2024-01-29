package com.sdu.provider;

import com.sdu.zrpc.framework.common.annotation.EnableZrpc;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ComponentScan("com.sdu.zrpc")
@MapperScan("com.sdu.zrpc")
@EnableZrpc(basePackages = "com.sdu.provider.impl")
@EnableAspectJAutoProxy
@EnableTransactionManagement
public class SpringProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringProviderApplication.class, args);
    }
}