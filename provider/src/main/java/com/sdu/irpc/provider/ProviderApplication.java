package com.sdu.irpc.provider;

import com.sdu.irpc.framework.core.config.IRpcBootstrap;

/**
 * 服务提供方，需要注册服务，启动服务
 */
public class ProviderApplication {

    public static void main(String[] args) {
        IRpcBootstrap.getInstance()
                .port(8097)
                .scan("com.sdu.irpc").start();
    }
}
