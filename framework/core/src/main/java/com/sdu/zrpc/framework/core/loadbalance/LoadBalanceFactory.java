package com.sdu.zrpc.framework.core.loadbalance;

import com.sdu.zrpc.framework.common.entity.ObjectWrapper;
import com.sdu.zrpc.framework.common.enums.LoadBalanceType;
import com.sdu.zrpc.framework.core.loadbalance.impl.ConsistentHashLoadBalance;
import com.sdu.zrpc.framework.core.loadbalance.impl.RandomLoadBalance;
import com.sdu.zrpc.framework.core.loadbalance.impl.ResponseTimeLoadBalance;
import com.sdu.zrpc.framework.core.loadbalance.impl.RoundRobinLoadBalance;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class LoadBalanceFactory {

    private static final ConcurrentHashMap<LoadBalanceType, ObjectWrapper<LoadBalance>> LOAD_BALANCER_CACHE = new ConcurrentHashMap<>(8);
    private static final ConcurrentHashMap<Byte, ObjectWrapper<LoadBalance>> LOAD_BALANCER_CODE_CACHE = new ConcurrentHashMap<>(8);

    static {
        ObjectWrapper<LoadBalance>[] loadBalances = new ObjectWrapper[]{
                new ObjectWrapper<>((byte) 1, LoadBalanceType.ROUND_ROBIN.name(), new RoundRobinLoadBalance()),
                new ObjectWrapper<>((byte) 2, LoadBalanceType.CONSISTENT_HASH.name(), new ConsistentHashLoadBalance()),
                new ObjectWrapper<>((byte) 3, LoadBalanceType.RESPONSE_TIME.name(), new ResponseTimeLoadBalance()),
                new ObjectWrapper<>((byte) 4, LoadBalanceType.RANDOM.name(), new RandomLoadBalance())
        };

        for (ObjectWrapper<LoadBalance> lb : loadBalances) {
            LOAD_BALANCER_CACHE.put(LoadBalanceType.valueOf(lb.getName()), lb);
            LOAD_BALANCER_CODE_CACHE.put(lb.getCode(), lb);
        }
    }


    /**
     * 使用工厂方法获取一个负载均衡器
     *
     * @param loadBalanceType 负载均衡器的类型
     * @return loadBalanceObjectWrapper
     */
    public static ObjectWrapper<LoadBalance> getLoadBalanceStrategy(LoadBalanceType loadBalanceType) {
        ObjectWrapper<LoadBalance> loadBalanceObjectWrapper = LOAD_BALANCER_CACHE.get(loadBalanceType);
        if (null == loadBalanceObjectWrapper) {
            log.error("未找到您配置的【{}】负载均衡器，默认选用轮询的负载均衡。", loadBalanceType);
            return LOAD_BALANCER_CACHE.get(LoadBalanceType.ROUND_ROBIN);
        }
        return loadBalanceObjectWrapper;
    }

    /**
     * 使用工厂方法获取一个负载均衡器
     *
     * @param loadBalanceType 负载均衡器的类型
     * @return loadBalanceObjectWrapper
     */
    public static ObjectWrapper<LoadBalance> getLoadBalanceStrategy(Byte loadBalanceType) {
        ObjectWrapper<LoadBalance> loadBalanceObjectWrapper = LOAD_BALANCER_CODE_CACHE.get(loadBalanceType);
        if (null == loadBalanceObjectWrapper) {
            log.error("未找到您配置的【{}】负载均衡器，默认选用轮询的负载均衡。", loadBalanceType);
            return LOAD_BALANCER_CACHE.get(LoadBalanceType.ROUND_ROBIN);
        }
        return loadBalanceObjectWrapper;
    }

    /**
     * 给工厂中新增一个负载均衡方式
     *
     * @param loadBalanceObjectWrapper 负载均衡类型的包装
     */
    public static void addLoadBalanceStrategy(ObjectWrapper<LoadBalance> loadBalanceObjectWrapper) {
        LOAD_BALANCER_CODE_CACHE.put(loadBalanceObjectWrapper.getCode(), loadBalanceObjectWrapper);
    }
}
