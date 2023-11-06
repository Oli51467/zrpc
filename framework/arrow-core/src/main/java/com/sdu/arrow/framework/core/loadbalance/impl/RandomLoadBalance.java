package com.sdu.arrow.framework.core.loadbalance.impl;

import com.sdu.arrow.framework.common.exception.LoadBalanceException;
import com.sdu.arrow.framework.core.loadbalance.AbstractLoadBalance;
import com.sdu.arrow.framework.core.loadbalance.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机的负载均衡策略
 */
@Slf4j
public class RandomLoadBalance extends AbstractLoadBalance {

    @Override
    protected Selector initSelector(List<InetSocketAddress> serviceList) {
        return new RandomSelector(serviceList);
    }

    private record RandomSelector(List<InetSocketAddress> serviceList) implements Selector {

        @Override
        public InetSocketAddress select() {
            if (serviceList.isEmpty()) {
                log.error("进行负载均衡选取节点时发现服务列表为空.");
                throw new LoadBalanceException();
            } else {
                int selectPosition = ThreadLocalRandom.current().nextInt(serviceList.size());
                InetSocketAddress socketAddress = serviceList.get(selectPosition);
                log.info("使用随机的负载均衡算法，select remote address: {}", socketAddress.getAddress().toString());
                return socketAddress;
            }
        }
    }
}
