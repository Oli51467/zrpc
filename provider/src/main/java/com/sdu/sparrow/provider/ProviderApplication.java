package com.sdu.sparrow.provider;

import com.sdu.sparrow.framework.core.config.RpcBootstrap;

/**
 * 服务提供方，需要注册服务，启动服务
 */
public class ProviderApplication {

    public static void main(String[] args) {
        RpcBootstrap.getInstance()
                .port(8097)
                .scanServices("com.sdu.sparrow").start();
    }
}
