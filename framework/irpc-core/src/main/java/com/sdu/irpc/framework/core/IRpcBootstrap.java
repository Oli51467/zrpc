package com.sdu.irpc.framework.core;

import com.sdu.irpc.framework.common.entity.ZooKeeperNode;
import com.sdu.irpc.framework.common.util.NetUtil;
import com.sdu.irpc.framework.common.util.ZookeeperUtil;
import com.sdu.irpc.framework.core.config.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

import static com.sdu.irpc.framework.common.constant.ZooKeeperConstant.*;

@Slf4j
public class IRpcBootstrap {

    private static IRpcBootstrap iRpcBootstrap = new IRpcBootstrap();

    private ProtocolConfig protocolConfig;
    private final Configuration configuration;
    private ZooKeeper zooKeeper;

    private IRpcBootstrap() {
        configuration = new Configuration();
    }

    public static IRpcBootstrap getInstance() {
        return iRpcBootstrap;
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
        zooKeeper = ZookeeperUtil.createZookeeperConnection();
        configuration.setRegistryConfig(registryConfig);
        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     *
     * @param protocolConfig 协议的封装
     * @return this当前实例
     */
    public IRpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        log.info("当前工程使用了：{}协议进行序列化", protocolConfig.toString());
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
        // 服务名称的节点
        String parentNode = getProviderNodePath(service.getInterface().getName());
        // 创建父节点
        if (!ZookeeperUtil.exists(zooKeeper, parentNode, null)) {
            ZooKeeperNode node = new ZooKeeperNode(parentNode, null);
            ZookeeperUtil.createNode(zooKeeper, node, null, CreateMode.PERSISTENT);
        }
        // 创建临时本机节点
        String finalNodePath = parentNode + SPLIT + NetUtil.getIp() + ":" + configuration.getPort();
        if (!ZookeeperUtil.exists(zooKeeper, finalNodePath, null)) {
            ZooKeeperNode node = new ZooKeeperNode(finalNodePath, null);
            ZookeeperUtil.createNode(zooKeeper, node, null, CreateMode.EPHEMERAL);
        }
        log.info("服务{}，已经被注册", service.getInterface().getName());
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
