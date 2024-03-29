package com.sdu.zrpc.framework.core.config;

import com.sdu.zrpc.framework.common.constant.DefaultBoostrapConfig;
import com.sdu.zrpc.framework.common.enums.CompressionType;
import com.sdu.zrpc.framework.common.enums.LoadBalanceType;
import com.sdu.zrpc.framework.common.enums.SerializationType;
import com.sdu.zrpc.framework.common.util.IdGenerator;
import com.sdu.zrpc.framework.core.protection.Breaker;
import com.sdu.zrpc.framework.core.protection.RateLimiter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Slf4j
public class Configuration implements DefaultBoostrapConfig {

    private Integer port = DEFAULT_PORT;

    private SerializationType serializationType = SerializationType.HESSIAN;

    private CompressionType compressionType = CompressionType.GZIP;

    private RegistryConfig registryConfig = new RegistryConfig(DEFAULT_REGISTRY_CONFIG);

    private IdGenerator idGenerator = new IdGenerator(1, 2);

    private LoadBalanceType loadBalanceType = LoadBalanceType.ROUND_ROBIN;

    // 为每一个ip配置一个限流器和熔断器
    private final Map<SocketAddress, RateLimiter> ipRateLimiter = new ConcurrentHashMap<>(16);
    private final Map<SocketAddress, Breaker> ipBreaker = new ConcurrentHashMap<>(16);

    public Configuration() {
    }

    public void setPort(Integer port) {
        this.port = port + 10000;
    }
}
