package com.sdu.irpc;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ZookeeperTest {

    private ZooKeeper zooKeeper;

    @Before
    public void createZk() {
        // 定义连接参数
        String connectString = "127.0.0.1:2181";
        // 定义超时时间
        int timeout = 10000;
//        try {
//            // new MyWatcher() 默认的watcher
//            zooKeeper = new ZooKeeper(connectString, timeout, new MyWatcher());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    @Test
    public void testCreatePNode() {
        try {
            String result = zooKeeper.create("/irpc", "hello".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println("result = " + result);
        } catch (
                KeeperException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zooKeeper != null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testDeletePNode() {
        try {
            zooKeeper.delete("/irpc", -1);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != zooKeeper) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
