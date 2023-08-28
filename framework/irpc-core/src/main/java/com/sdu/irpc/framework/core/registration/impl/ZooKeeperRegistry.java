package com.sdu.irpc.framework.core.registration.impl;

import com.sdu.irpc.framework.common.entity.ZooKeeperNode;
import com.sdu.irpc.framework.common.util.NetUtil;
import com.sdu.irpc.framework.common.util.ZookeeperUtil;
import com.sdu.irpc.framework.core.IRpcBootstrap;
import com.sdu.irpc.framework.core.config.ServiceConfig;
import com.sdu.irpc.framework.core.registration.AbstractRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import static com.sdu.irpc.framework.common.constant.ZooKeeperConstant.getPath;
import static com.sdu.irpc.framework.common.constant.ZooKeeperConstant.getProviderNodePath;

@Slf4j
public class ZooKeeperRegistry extends AbstractRegistry {

    private ZooKeeper zooKeeper;

    public ZooKeeperRegistry() {
        this.zooKeeper = ZookeeperUtil.createZookeeperConnection();
    }

    public ZooKeeperRegistry(String connectionAddr, int timeout) {
        this.zooKeeper = ZookeeperUtil.createZookeeperConnection(connectionAddr, timeout);
    }

    @Override
    public void register(ServiceConfig<?> service) {
        // 服务名称的节点 最后一层是方法的全类名
        String parentNode = getProviderNodePath(service.getInterface().getName());
        // 创建父节点
        if (!ZookeeperUtil.exists(zooKeeper, parentNode, null)) {
            ZooKeeperNode node = new ZooKeeperNode(parentNode, null);
            ZookeeperUtil.createNode(zooKeeper, node, null, CreateMode.PERSISTENT);
        }
        // 创建临时本机节点
        String finalNodePath = getPath(parentNode, NetUtil.getIp(IRpcBootstrap.getInstance().getConfiguration().getPort()));
        if (!ZookeeperUtil.exists(zooKeeper, finalNodePath, null)) {
            ZooKeeperNode node = new ZooKeeperNode(finalNodePath, null);
            ZookeeperUtil.createNode(zooKeeper, node, null, CreateMode.EPHEMERAL);
        }
        log.info("服务{}，已经被注册", service.getInterface().getName());
    }
}
