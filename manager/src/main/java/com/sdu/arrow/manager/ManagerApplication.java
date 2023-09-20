package com.sdu.arrow.manager;

import com.sdu.arrow.framework.common.entity.ZooKeeperNode;
import com.sdu.arrow.framework.common.util.ZookeeperUtil;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

import static com.sdu.arrow.framework.common.constant.ZooKeeperConstant.*;

public class ManagerApplication {

    public static void main(String[] args) {
        ZooKeeper zooKeeper = ZookeeperUtil.createZookeeperConnection();
        // 定义持久节点和数据
        ZooKeeperNode baseNode = new ZooKeeperNode(BASE_PATH, null);
        ZooKeeperNode providersNode = new ZooKeeperNode(getBaseProvidersPath(), null);
        ZooKeeperNode clientsNode = new ZooKeeperNode(getBaseClientsPath(), null);
        // 创建持久节点
        List.of(baseNode, providersNode, clientsNode).forEach(node -> ZookeeperUtil.createNode(zooKeeper, node, null, CreateMode.PERSISTENT));
        ZookeeperUtil.close(zooKeeper);
    }
}