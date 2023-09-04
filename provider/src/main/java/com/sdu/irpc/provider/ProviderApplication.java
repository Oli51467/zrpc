package com.sdu.irpc.provider;

import com.sdu.irpc.framework.core.IRpcBootstrap;

/**
 * 服务提供方，需要注册服务，启动服务
 */
public class ProviderApplication {

    public static void main(String[] args) {
        IRpcBootstrap.getInstance()
                .application("p1")
                .port(8098)
                .scan("com.sdu.irpc").start();
    }
}
