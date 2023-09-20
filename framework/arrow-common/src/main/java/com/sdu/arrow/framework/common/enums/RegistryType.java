package com.sdu.arrow.framework.common.enums;

import lombok.Getter;

@Getter
public enum RegistryType {

    ZOOKEEPER("zookeeper"),
    NACOS("nacos");

    RegistryType(String name) {
        this.name = name;
    }

    private final String name;
}
