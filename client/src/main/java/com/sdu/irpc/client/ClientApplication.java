package com.sdu.irpc.client;

import com.sdu.irpc.api.Greet;
import com.sdu.irpc.framework.core.IRpcBootstrap;
import com.sdu.irpc.framework.core.config.ReferenceConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientApplication {

    public static void main(String[] args) {
        ReferenceConfig<Greet> reference = new ReferenceConfig<>();
        reference.setTargetInterface(Greet.class);
        reference.setAppName("p1");
        /* 获取代理对象，使用ReferenceConfig进行封装，代理：
         * 1. 连接注册中心
         * 2. 拉取服务列表
         * 3. 选择一个服务并建立连接
         * 4. 发送请求，获得结果
         */
        IRpcBootstrap.getInstance().application("client");
        for (int i = 0; i < 10; i++) {
            log.info("远程调用返回值: {}", reference.get().greet("Client say hi"));
        }
    }
}