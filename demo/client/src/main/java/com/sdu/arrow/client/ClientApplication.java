package com.sdu.arrow.client;

import com.sdu.arrow.demo.api.Greet;
import com.sdu.arrow.framework.common.enums.SerializationType;
import com.sdu.arrow.framework.core.config.RpcBootstrap;
import com.sdu.arrow.framework.core.config.ReferenceConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientApplication {

    public static void main(String[] args) {
        // 获取代理对象，使用ReferenceConfig进行封装
        ReferenceConfig<Greet> reference = new ReferenceConfig<>();
        reference.setTargetInterface(Greet.class);
        Greet proxy = reference.get();
        RpcBootstrap.getInstance().serialize(SerializationType.HESSIAN);
        /* 代理：
         * 1. 连接注册中心
         * 2. 拉取服务列表
         * 3. 选择一个服务并建立连接
         * 4. 发送请求，获得结果
         */
        log.info("远程调用返回值: {}", proxy.greet("Client say hi"));
    }
}