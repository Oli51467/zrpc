package com.sdu.irpc.framework.core.watcher;

import com.sdu.irpc.framework.common.exception.NetworkException;
import com.sdu.irpc.framework.core.config.IRpcBootstrap;
import com.sdu.irpc.framework.core.netty.NettyBoostrapInitializer;
import com.sdu.irpc.framework.core.registry.Registry;
import io.netty.channel.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

@Slf4j
public class UpAndDownWatcher implements Watcher {

    @SneakyThrows
    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeChildrenChanged) {
            log.info("检测到服务【{}】下有节点变化", event.getPath());
            Thread.sleep(500);
            String[] pathArgs = event.getPath().split("/");
            String serviceName = pathArgs[pathArgs.length - 1];
            String appName = pathArgs[pathArgs.length - 2];
            Registry registry = IRpcBootstrap.getInstance().getRegistry();
            List<InetSocketAddress> addressList = registry.discover(appName, serviceName);
            // 处理新增的节点
            for (InetSocketAddress address : addressList) {
                if (!IRpcBootstrap.CHANNEL_CACHE.containsKey(address)) {
                    Channel channel;
                    try {
                        channel = NettyBoostrapInitializer.getBootstrap().connect(address).sync().channel();
                        IRpcBootstrap.CHANNEL_CACHE.put(address, channel);
                    } catch (InterruptedException e) {
                        throw new NetworkException("获取通道连接时发生了异常。");
                    }
                }
            }
            // 处理下线的节点
            for (Map.Entry<InetSocketAddress, Channel> entry : IRpcBootstrap.CHANNEL_CACHE.entrySet()) {
                if (!addressList.contains(entry.getKey())) {
                    IRpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                }
            }
            // 重新负载均衡
            IRpcBootstrap.getInstance().getLoadBalancer().reload(serviceName, addressList);
        }
    }
}
