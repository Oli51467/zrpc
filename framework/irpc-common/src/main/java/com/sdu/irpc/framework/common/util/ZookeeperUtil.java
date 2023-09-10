package com.sdu.irpc.framework.common.util;

import com.sdu.irpc.framework.common.constant.ZooKeeperConstant;
import com.sdu.irpc.framework.common.entity.ZooKeeperNode;
import com.sdu.irpc.framework.common.exception.ZooKeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ZookeeperUtil {

    /**
     * 使用默认配置创建zookeeper实例
     *
     * @return zookeeper实例
     */
    public static ZooKeeper createZookeeperConnection() {
        return createZookeeperConnection(ZooKeeperConstant.DEFAULT_ZK_CONNECT, ZooKeeperConstant.TIME_OUT);
    }

    public static ZooKeeper createZookeeperConnection(String connectionAddr, int timeout) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            // 创建zookeeper实例，建立连接
            ZooKeeper zooKeeper = new ZooKeeper(connectionAddr, timeout, event -> {
                // 只有连接成功才放行
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    log.info("ZooKeeper连接成功");
                    countDownLatch.countDown();
                }
            });

            countDownLatch.await();
            return zooKeeper;
        } catch (IOException | InterruptedException e) {
            log.error("创建zookeeper连接发生异常：", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建一个ZooKeeper节点
     *
     * @param zooKeeper  zooKeeper实例
     * @param node       节点
     * @param watcher    watcher实例
     * @param createMode 节点的类型
     * @return true: 成功创建  false: 已经存在  异常：抛出
     */
    public static Boolean createNode(ZooKeeper zooKeeper, ZooKeeperNode node, Watcher watcher, CreateMode createMode) {
        try {
            // 判断节点是否存在
            if (zooKeeper.exists(node.getNodePath(), watcher) == null) {
                String result = zooKeeper.create(node.getNodePath(), node.getData(), ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                log.info("节点【{}】，成功创建。", result);
                return true;
            } else {
                log.info("节点【{}】已经存在，无需创建。", node.getNodePath());
                return false;
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("创建基础目录时发生异常：", e);
            throw new ZooKeeperException();
        }
    }

    public static void deleteNode(ZooKeeper zooKeeper, String path, Watcher watcher, Integer version) {
        try {
            if (zooKeeper.exists(path, watcher) != null) {
                zooKeeper.delete(path, version);
            }
        } catch (InterruptedException | KeeperException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断节点是否存在
     *
     * @param zooKeeper zooKeeper实例
     * @param node      节点路劲
     * @param watcher   watcher
     * @return ture 存在 | false 不存在
     */
    public static boolean exists(ZooKeeper zooKeeper, String node, Watcher watcher) {
        try {
            return zooKeeper.exists(node, watcher) != null;
        } catch (KeeperException | InterruptedException e) {
            log.error("判断节点[{}]是否存在是发生异常", node, e);
            throw new ZooKeeperException(e);
        }
    }

    /**
     * 关闭zookeeper的方法
     *
     * @param zooKeeper zooKeeper实例
     */
    public static void close(ZooKeeper zooKeeper) {
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            log.error("关闭zookeeper时发生问题：", e);
            throw new ZooKeeperException();
        }
    }

    /**
     * 查询一个节点的子元素
     *
     * @param zooKeeper   zk实例
     * @param serviceNode 服务节点
     * @return 子元素列表
     */
    public static List<String> getChildren(ZooKeeper zooKeeper, String serviceNode, Watcher watcher) {
        try {
            return zooKeeper.getChildren(serviceNode, watcher);
        } catch (KeeperException | InterruptedException e) {
            log.error("获取节点【{}】的子元素时发生异常.", serviceNode, e);
            throw new ZooKeeperException(e);
        }
    }
}
