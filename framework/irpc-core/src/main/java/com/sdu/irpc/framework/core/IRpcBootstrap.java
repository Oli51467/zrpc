package com.sdu.irpc.framework.core;

import com.sdu.irpc.framework.common.util.ZookeeperUtil;
import com.sdu.irpc.framework.core.config.Configuration;
import com.sdu.irpc.framework.core.config.ReferenceConfig;
import com.sdu.irpc.framework.core.config.RegistryConfig;
import com.sdu.irpc.framework.core.config.ServiceConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

@Slf4j
public class IRpcBootstrap {

    private static IRpcBootstrap iRpcBootstrap = new IRpcBootstrap();

    private final Configuration configuration;
    private ZooKeeper zooKeeper;

    private IRpcBootstrap() {
        configuration = new Configuration();
    }

    public static IRpcBootstrap getInstance() {
        return iRpcBootstrap;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 用来定义当前应用的名字
     *
     * @param applicationName 应用的名字
     * @return this当前实例
     */
    public IRpcBootstrap application(String applicationName) {
        configuration.setApplicationName(applicationName);
        return this;
    }

    /**
     * 用来配置一个注册中心
     *
     * @param registryConfig 注册中心
     * @return this当前实例
     */
    public IRpcBootstrap registry(RegistryConfig registryConfig) {
        configuration.setRegistryConfig(registryConfig);
        zooKeeper = ZookeeperUtil.createZookeeperConnection();
        return this;
    }

    public IRpcBootstrap registry() {
        zooKeeper = ZookeeperUtil.createZookeeperConnection();
        return this;
    }

    /**
     * 配置服务使用的序列化协议
     *
     * @param serializeType 协议的封装
     * @return this当前实例
     */
    public IRpcBootstrap serialize(String serializeType) {
        configuration.setSerialization(serializeType);
        log.info("当前工程使用了：{}协议进行序列化", serializeType);
        return this;
    }

    /**
     * 配置服务使用的压缩方式
     *
     * @param compression 传输压缩方式
     * @return this当前实例
     */
    public IRpcBootstrap compression(String compression) {
        configuration.setCompression(compression);
        log.info("当前工程使用了：{}进行压缩", compression);
        return this;
    }

    /**
     * 配置服务使用组名
     *
     * @param groupName 组名
     * @return this当前实例
     */
    public IRpcBootstrap group(String groupName) {
        configuration.setGroupName(groupName);
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start()  {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 发布服务，将接口的实现注册到服务中心
     *
     * @param service 封装的需要发布的服务
     * @return this当前实例
     */
    public IRpcBootstrap publish(ServiceConfig<?> service) {
        configuration.getRegistryConfig().getRegistry().register(service);
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
