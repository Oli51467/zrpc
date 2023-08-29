package com.sdu.irpc.framework.core.config;

import com.sdu.irpc.framework.common.constant.ZooKeeperConstant;
import com.sdu.irpc.framework.common.enums.RegistryType;
import com.sdu.irpc.framework.common.exception.DiscoveryException;
import com.sdu.irpc.framework.core.registry.Registry;
import com.sdu.irpc.framework.core.registry.impl.ZooKeeperRegistry;

public class RegistryConfig {

    private final String connectionName;

    public RegistryConfig(String connectionName) {
        this.connectionName = connectionName;
    }

    public Registry getRegistry() {
        // 获取注册中心的类型
        String registryType = getRegistryType(connectionName, true).toLowerCase().trim();
        if (RegistryType.ZOOKEEPER.getName().equals(registryType)) {
            String host = getRegistryType(connectionName, false);
            return new ZooKeeperRegistry(host, ZooKeeperConstant.TIME_OUT);
        }
        throw new DiscoveryException("未发现注册中心");
    }

    private String getRegistryType(String connectionName, boolean ifType) {
        String[] typeAndHost = connectionName.split("://");
        if (typeAndHost.length != 2) {
            throw new RuntimeException("非法的注册中心url");
        }
        if (ifType) {
            return typeAndHost[0];
        } else {
            return typeAndHost[1];
        }
    }
}
