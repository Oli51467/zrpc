package com.sdu.irpc.test;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class IZookeeperWatcher implements Watcher {

    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.None) {
            if (event.getState() == Event.KeeperState.SyncConnected) {
                System.out.println("Zookeeper连接成功！");
            } else if (event.getState() == Event.KeeperState.AuthFailed) {
                System.out.println("Zookeeper连接失败！");
            } else if (event.getState() == Event.KeeperState.Disconnected) {
                System.out.println("Zookeeper断开连接");
            }
        } else if (event.getType() == Event.EventType.NodeCreated) {
            System.out.println(event.getPath() + "被创建了");
        } else if (event.getType() == Event.EventType.NodeDeleted) {
            System.out.println(event.getPath() + "被删除了了");
        } else if (event.getType() == Event.EventType.NodeDataChanged) {
            System.out.println(event.getPath() + "节点的数据改变了");
        } else if (event.getType() == Event.EventType.NodeChildrenChanged) {
            System.out.println(event.getPath() + "子节点发生了变化");
        }
    }
}
