package com.sdu.irpc.framework.core;

import com.sdu.irpc.framework.core.config.ProtocolConfig;
import com.sdu.irpc.framework.core.config.ReferenceConfig;
import com.sdu.irpc.framework.core.config.RegistryConfig;
import com.sdu.irpc.framework.core.config.ServiceConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class IRpcBootstrap {

    private static IRpcBootstrap iRpcBootstrap = new IRpcBootstrap();

    private IRpcBootstrap() {

    }

    public static IRpcBootstrap getInstance() {
        return iRpcBootstrap;
    }

    /**
     * 用来定义当前应用的名字
     *
     * @param appName 应用的名字
     * @return this当前实例
     */
    public IRpcBootstrap application(String appName) {
        return this;
    }

    /**
     * 用来配置一个注册中心
     *
     * @param registryConfig 注册中心
     * @return this当前实例
     */
    public IRpcBootstrap registry(RegistryConfig registryConfig) {
        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     *
     * @param protocolConfig 协议的封装
     * @return this当前实例
     */
    public IRpcBootstrap protocol(ProtocolConfig protocolConfig) {
        if (log.isDebugEnabled()) {
            log.debug("当前工程使用了：{}协议进行序列化", protocolConfig.toString());
        }
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {

    }

    /**
     * 发布服务，将接口的实现注册到服务中心
     *
     * @param service 封装的需要发布的服务
     * @return this当前实例
     */
    public IRpcBootstrap publish(ServiceConfig<?> service) {
        if (log.isDebugEnabled()) {
            log.debug("服务{}，已经被注册", service.getInterface().getName());
        }
        return this;
    }

    /**
     * 批量发布
     *
     * @param services 封装的需要发布的服务集合
     * @return this当前实例
     */
    public IRpcBootstrap publish(List<?> services) {
        return this;
    }

    public IRpcBootstrap reference(ReferenceConfig<?> reference) {

        // 在这个方法里我们是否可以拿到相关的配置项-注册中心
        // 配置reference，将来调用get方法时，方便生成代理对象
        return this;

    }
}
