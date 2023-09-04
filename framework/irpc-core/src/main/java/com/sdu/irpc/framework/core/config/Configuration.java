package com.sdu.irpc.framework.core.config;

import com.sdu.irpc.framework.common.constant.DefaultBoostrapConfig;
import com.sdu.irpc.framework.common.enums.CompressionType;
import com.sdu.irpc.framework.common.enums.LoadBalancerType;
import com.sdu.irpc.framework.common.enums.SerializationType;
import com.sdu.irpc.framework.common.util.IdGenerator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Configuration implements DefaultBoostrapConfig {

    private Integer port = DEFAULT_PORT;

    private String applicationName = DEFAULT_APPLICATION_NAME;

    private String groupName = DEFAULT_GROUP_NAME;

    private SerializationType serializationType = SerializationType.JDK;

    private CompressionType compressionType = CompressionType.GZIP;

    private RegistryConfig registryConfig = new RegistryConfig(DEFAULT_REGISTRY_CONFIG);

    private IdGenerator idGenerator = new IdGenerator();

    private LoadBalancerType loadBalancer = LoadBalancerType.ROUND_ROBIN;

    private ServiceConfig<?> serviceConfig;
}
