package com.sdu.irpc.framework.core.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡器的接口
 */
public interface LoadBalancer {

    /**
     * 根据服务名获取一个可用的服务
     *
     * @param path 服务路径
     * @return 服务地址
     */
    InetSocketAddress selectService(String appName, String path);

    /**
     * 当感知节点发生了动态上下线，我们需要重新进行负载均衡
     *
     * @param path 服务路径
     */
    void reload(String path, List<InetSocketAddress> addresses);
}
