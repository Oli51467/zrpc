package com.sdu.irpc.framework.core.loadbalancer;

import com.sdu.irpc.framework.common.entity.ObjectWrapper;
import com.sdu.irpc.framework.common.enums.LoadBalancerType;
import com.sdu.irpc.framework.core.loadbalancer.impl.ConsistentHashLoadBalancer;
import com.sdu.irpc.framework.core.loadbalancer.impl.ResponseTimeLoadBalancer;
import com.sdu.irpc.framework.core.loadbalancer.impl.RoundRobinLoadBalancer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class LoadBalancerFactory {

    private static final ConcurrentHashMap<LoadBalancerType, ObjectWrapper<LoadBalancer>> LOAD_BALANCER_CACHE = new ConcurrentHashMap<>(8);

    static {
        ObjectWrapper<LoadBalancer> roundRobin = new ObjectWrapper<>((byte) 1, LoadBalancerType.ROUND_ROBIN.name(), new RoundRobinLoadBalancer());
        ObjectWrapper<LoadBalancer> consistentHash = new ObjectWrapper<>((byte) 2, LoadBalancerType.CONSISTENT_HASH.name(), new ConsistentHashLoadBalancer());
        ObjectWrapper<LoadBalancer> responseTime = new ObjectWrapper<>((byte) 3, LoadBalancerType.RESPONSE_TIME.name(), new ResponseTimeLoadBalancer());
        LOAD_BALANCER_CACHE.put(LoadBalancerType.ROUND_ROBIN, roundRobin);
        LOAD_BALANCER_CACHE.put(LoadBalancerType.CONSISTENT_HASH, consistentHash);
        LOAD_BALANCER_CACHE.put(LoadBalancerType.RESPONSE_TIME, responseTime);
    }

    /**
     * 使用工厂方法获取一个负载均衡器
     *
     * @param loadbalancerType 负载均衡器的类型
     * @return LoadbalancerWrapper
     */
    public static ObjectWrapper<LoadBalancer> getLoadbalancer(LoadBalancerType loadbalancerType) {
        ObjectWrapper<LoadBalancer> loadBalancerObjectWrapper = LOAD_BALANCER_CACHE.get(loadbalancerType);
        if (null == loadBalancerObjectWrapper) {
            log.error("未找到您配置的【{}】负载均衡器，默认选用轮询的负载均衡。", loadbalancerType);
            return LOAD_BALANCER_CACHE.get(LoadBalancerType.ROUND_ROBIN);
        }
        return loadBalancerObjectWrapper;
    }
}
