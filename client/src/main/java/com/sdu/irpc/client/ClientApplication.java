package com.sdu.irpc.client;

import com.sdu.irpc.api.Greet;
import com.sdu.irpc.framework.common.enums.SerializationType;
import com.sdu.irpc.framework.core.config.IRpcBootstrap;
import com.sdu.irpc.framework.core.config.ReferenceConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientApplication {

    public static void main(String[] args) {
        // 获取代理对象，使用ReferenceConfig进行封装
        ReferenceConfig<Greet> reference = new ReferenceConfig<>();
        reference.setTargetInterface(Greet.class);
        reference.setAppName("p1");
        reference.setPath("greet.echo");
        Greet proxy = reference.get();
        IRpcBootstrap.getInstance()
                .serialize(SerializationType.HESSIAN);
        /* 代理：
         * 1. 连接注册中心
         * 2. 拉取服务列表
         * 3. 选择一个服务并建立连接
         * 4. 发送请求，获得结果
         */
        while (true) {
            for (int i = 0; i < 5; i++) {
                log.info("远程调用返回值: {}", proxy.greet("Client say hi"));
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}