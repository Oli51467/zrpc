package com.sdu.irpc.framework.core.loadbalancer;

import com.sdu.irpc.framework.core.config.IRpcBootstrap;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractLoadBalancer implements LoadBalancer {

    // 一个服务路径会匹配一个selector
    private Map<String, Selector> selectorCache = new ConcurrentHashMap<>(8);

    @Override
    public InetSocketAddress selectService(String appName, String path) {
        // 优先从cache中获取一个选择器
        Selector selector = selectorCache.get(path);
        // 如果没有，就需要为这个service创建一个selector
        if (null == selector) {
            // 注册中心服务发现所有可用的节点
            List<InetSocketAddress> serviceList = IRpcBootstrap.getInstance().getRegistry().discover(appName, path);
            // 具体的选择逻辑由子类实现
            selector = initSelector(serviceList);
            // 将select放入缓存当中
            selectorCache.put(path, selector);
        }
        // 执行selector的选择逻辑选择一个节点
        return selector.select();
    }

    @Override
    public synchronized void reload(String path, List<InetSocketAddress> serviceList) {
        selectorCache.put(path, initSelector(serviceList));
    }

    protected abstract Selector initSelector(List<InetSocketAddress> serviceList);
}
