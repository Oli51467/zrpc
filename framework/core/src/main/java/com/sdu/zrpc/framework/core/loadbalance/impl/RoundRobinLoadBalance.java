package com.sdu.zrpc.framework.core.loadbalance.impl;

import com.sdu.zrpc.framework.common.exception.LoadBalanceException;
import com.sdu.zrpc.framework.core.loadbalance.AbstractLoadBalance;
import com.sdu.zrpc.framework.core.loadbalance.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RoundRobinLoadBalance extends AbstractLoadBalance {

    @Override
    protected Selector initSelector(List<InetSocketAddress> serviceList) {
        return new RoundRobinSelector(serviceList);
    }

    /**
     * RoundRobinSelector 轮训负载均衡选择器
     * 如果内部类不会引用到外部类，强烈建议使用静态内部类节省资源，减少内部类其中的一个指向外部类的引用。
     */
    private static class RoundRobinSelector implements Selector {
        private final List<InetSocketAddress> serviceList;
        private final AtomicInteger index;

        public RoundRobinSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
            this.index = new AtomicInteger(0);
        }

        @Override
        public InetSocketAddress select() {
            if (null == serviceList || serviceList.isEmpty()) {
                log.error("进行负载均衡选取节点时发现服务列表为空.");
                throw new LoadBalanceException();
            }
            InetSocketAddress address = serviceList.get(index.get());
            if (index.get() == serviceList.size() - 1) {
                index.set(0);
            } else {
                index.incrementAndGet();
            }
            return address;
        }
    }
}
