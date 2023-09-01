package com.sdu.irpc.provider;

import com.sdu.irpc.api.Greet;
import com.sdu.irpc.framework.core.IRpcBootstrap;
import com.sdu.irpc.framework.core.config.ServiceConfig;
import com.sdu.irpc.provider.impl.GreetImpl;

/**
 * 服务提供方，需要注册服务，启动服务
 */
public class ProviderApplication {

    public static void main(String[] args) {
        // 封装要发布的服务
        ServiceConfig<Greet> service = new ServiceConfig<>();
        service.setInterface(Greet.class);
        service.setReference(new GreetImpl());
        service.setApplicationName("p1");
        // 配置应用名、序列化协议、压缩方式并发布服务
        IRpcBootstrap.getInstance()
                .application("p1")
                .port(8091)
                .publish(service)
                .start();
    }
}
