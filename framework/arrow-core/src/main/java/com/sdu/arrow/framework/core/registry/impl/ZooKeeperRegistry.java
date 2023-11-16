package com.sdu.arrow.framework.core.registry.impl;

import com.sdu.arrow.framework.common.entity.ZooKeeperNode;
import com.sdu.arrow.framework.common.exception.DiscoveryException;
import com.sdu.arrow.framework.common.util.NetUtil;
import com.sdu.arrow.framework.common.util.ZookeeperUtil;
import com.sdu.arrow.framework.core.config.RpcBootstrap;
import com.sdu.arrow.framework.core.config.ServiceConfig;
import com.sdu.arrow.framework.core.registry.AbstractRegistry;
import com.sdu.arrow.framework.core.watcher.UpAndDownWatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.sdu.arrow.framework.common.constant.ZooKeeperConstant.getPath;
import static com.sdu.arrow.framework.common.constant.ZooKeeperConstant.getProviderNodePath;

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
    public void register(ServiceConfig service) {
        // 服务名称的节点 最后一层是方法的全类名
        // 路径是 /providers/appName/methodName/ip1, ip2,...
        String applicationNode = getProviderNodePath(service.getApplicationName());
        // 创建父节点(应用节点)
        if (!ZookeeperUtil.exists(zooKeeper, applicationNode, null)) {
            ZooKeeperNode node = new ZooKeeperNode(applicationNode, applicationNode.getBytes(StandardCharsets.UTF_8));
            ZookeeperUtil.createNode(zooKeeper, node, null, CreateMode.PERSISTENT);
            log.info("应用节点创建");
        }
        String nodePath = getPath(applicationNode, service.getPath());
        // 创建父节点(路径节点)
        if (!ZookeeperUtil.exists(zooKeeper, nodePath, null)) {
            ZooKeeperNode node = new ZooKeeperNode(nodePath, nodePath.getBytes(StandardCharsets.UTF_8));
            ZookeeperUtil.createNode(zooKeeper, node, null, CreateMode.PERSISTENT);
            log.info("路径节点创建");
        }
        // 创建临时本机节点(ip节点)
        String remoteIp = NetUtil.getIp(RpcBootstrap.getInstance().getConfiguration().getPort());
        String finalNodePath = getPath(nodePath, remoteIp);
        if (!ZookeeperUtil.exists(zooKeeper, finalNodePath, null)) {
            ZooKeeperNode node = new ZooKeeperNode(finalNodePath, remoteIp.getBytes(StandardCharsets.UTF_8));
            ZookeeperUtil.createNode(zooKeeper, node, null, CreateMode.EPHEMERAL);
            log.info("ip节点创建");
        }
        log.info("服务{}注册成功", service.getPath());
    }

    /**
     * 从注册中心拉取合适的服务列表
     * @param path 服务名称
     * @return 服务列表
     */
    @Override
    public List<InetSocketAddress> discover(String appName, String path) {
        // 找到服务对应的节点
        String serviceNode = getProviderNodePath(appName, path);
        // 从zk中获取他的子节点
        List<String> children = ZookeeperUtil.getChildren(zooKeeper, serviceNode, new UpAndDownWatcher());
        // 获取了所有的可用的服务列表
        List<InetSocketAddress> inetSocketAddresses = children.stream().map(address -> {
            String[] ipAndPort = address.split(":");
            String ip = ipAndPort[0];
            int port = Integer.parseInt(ipAndPort[1]);
            return new InetSocketAddress(ip, port);
        }).toList();

        if (inetSocketAddresses.isEmpty()) {
            throw new DiscoveryException("未发现可用的服务主机");
        }
        return inetSocketAddresses;
    }
}
