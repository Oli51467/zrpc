package com.sdu.irpc.framework.common.constant;

public interface DefaultBoostrapConfig {

    Integer DEFAULT_PORT = 8090;

    String DEFAULT_APPLICATION_NAME = "default";

    String DEFAULT_GROUP_NAME = "default";

    String DEFAULT_SERIALIZATION = "jdk";

    String DEFAULT_COMPRESSION = "gzip";

    String DEFAULT_REGISTRY_CONFIG = "zookeeper://127.0.0.1:2181";
}
