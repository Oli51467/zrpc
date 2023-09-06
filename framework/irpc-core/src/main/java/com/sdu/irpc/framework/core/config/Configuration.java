package com.sdu.irpc.framework.core.config;

import com.sdu.irpc.framework.common.constant.DefaultBoostrapConfig;
import com.sdu.irpc.framework.common.enums.CompressionType;
import com.sdu.irpc.framework.common.enums.LoadBalancerType;
import com.sdu.irpc.framework.common.enums.SerializationType;
import com.sdu.irpc.framework.common.util.IdGenerator;
import com.sdu.irpc.framework.core.protection.Breaker;
import com.sdu.irpc.framework.core.protection.Limiter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Slf4j
public class Configuration implements DefaultBoostrapConfig {

    private Integer port = DEFAULT_PORT;

    private SerializationType serializationType = SerializationType.JDK;

    private CompressionType compressionType = CompressionType.GZIP;

    private RegistryConfig registryConfig = new RegistryConfig(DEFAULT_REGISTRY_CONFIG);

    private IdGenerator idGenerator = new IdGenerator();

    private LoadBalancerType loadBalancer = LoadBalancerType.ROUND_ROBIN;

    // 为每一个ip配置一个限流器和熔断器
    private final Map<SocketAddress, Limiter> ipRateLimiter = new ConcurrentHashMap<>(16);
    private final Map<SocketAddress, Breaker> ipBreaker = new ConcurrentHashMap<>(16);
}
