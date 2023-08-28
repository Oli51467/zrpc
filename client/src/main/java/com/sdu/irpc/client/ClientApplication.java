package com.sdu.irpc.client;

import com.sdu.irpc.api.Greet;
import com.sdu.irpc.framework.core.config.ReferenceConfig;
import com.sdu.irpc.framework.core.config.RegistryConfig;
import com.sdu.irpc.framework.core.IRpcBootstrap;

public class ClientApplication {

    public static void main(String[] args) {
        ReferenceConfig<Greet> reference = new ReferenceConfig<>();
        reference.setInterface(Greet.class);

        // 获取代理对象，使用ReferenceConfig进行封装

        /*
         * 代理做的工作
         * 1. 连接注册中心
         * 2. 拉取服务列表
         * 3. 选择一个服务并建立连接
         * 4. 发送请求，获得结果
         */
        IRpcBootstrap.getInstance()
                .application("client")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .reference(reference);
        Greet greet = reference.get();
        greet.greet("Client say hi");
    }
}