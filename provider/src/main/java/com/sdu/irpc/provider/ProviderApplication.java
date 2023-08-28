package com.sdu.irpc.provider;

import com.sdu.irpc.api.Greet;
import com.sdu.irpc.framework.core.config.ProtocolConfig;
import com.sdu.irpc.framework.core.config.RegistryConfig;
import com.sdu.irpc.framework.core.IRpcBootstrap;
import com.sdu.irpc.framework.core.config.ServiceConfig;
import com.sdu.irpc.provider.impl.GreetImpl;

/**
 * 服务提供方，需要注册服务，启动服务
 */
public class ProviderApplication {

    public static void main(String[] args) {

        // 1.封装要发布的服务
        ServiceConfig<Greet> service = new ServiceConfig<>();
        service.setInterface(Greet.class);
        service.setReference(new GreetImpl());

        // 2.定义注册中心

        // 3.通过启动引导程序，启动服务提供方
        // 配置 -- 应用的名称 -- 注册中心 -- 序列化协议 -- 压缩方式
        // 发布服务
        IRpcBootstrap.getInstance()
                .application("provider")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .protocol(new ProtocolConfig("jdk"))
                .publish(service)
                .start();
    }
}
