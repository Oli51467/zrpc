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
     * @param serviceName 服务名称
     * @return 服务地址
     */
    InetSocketAddress selectService(String serviceName);

    /**
     * 当感知节点发生了动态上下线，我们需要重新进行负载均衡
     *
     * @param serviceName 服务的名称
     */
    void reloadService(String serviceName, List<InetSocketAddress> addresses);
}
