package com.sdu.irpc.framework.core.config;

import com.sdu.irpc.framework.common.constant.DefaultBoostrapConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Configuration implements DefaultBoostrapConfig {

    private Integer port = DEFAULT_PORT;

    private String applicationName = DEFAULT_APPLICATION_NAME;

    private String groupName = DEFAULT_GROUP_NAME;

    private String serializationType = DEFAULT_SERIALIZATION;

    private String compressionType = DEFAULT_COMPRESSION;

    private RegistryConfig registryConfig = new RegistryConfig(DEFAULT_REGISTRY_CONFIG);
}
