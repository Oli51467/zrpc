package com.sdu.irpc.framework.core.loadbalancer;

import com.sdu.irpc.framework.core.IRpcBootstrap;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractLoadBalancer implements LoadBalancer {

    // 一个服务会匹配一个selector
    private Map<String, Selector> selectorCache = new ConcurrentHashMap<>(8);

    @Override
    public InetSocketAddress selectService(String serviceName) {
        // 优先从cache中获取一个选择器
        Selector selector = selectorCache.get(serviceName);
        // 如果没有，就需要为这个service创建一个selector
        if (null == selector) {
            // 注册中心服务发现所有可用的节点
            List<InetSocketAddress> serviceList = IRpcBootstrap.getInstance()
                    .getConfiguration().getRegistryConfig().getRegistry().discover(serviceName);
            // 具体的选择逻辑由子类实现
            selector = getSelector(serviceList);
            // 将select放入缓存当中
            selectorCache.put(serviceName, selector);
        }
        // 执行selector的选择逻辑选择一个节点
        return selector.select();
    }

    @Override
    public void reloadService(String serviceName, List<InetSocketAddress> addresses) {

    }

    protected abstract Selector getSelector(List<InetSocketAddress> serviceList);
}
