package com.sdu.sparrow.framework.common.constant;

public class ZooKeeperConstant {

    // zookeeper的默认连接地址
    public static final String DEFAULT_ZK_CONNECT = "127.0.0.1:2181";

    // zookeeper默认的超时时间
    public static final int TIME_OUT = 10000;

    // 服务提供方和调用方在注册中心的基础路径
    public static final String BASE_PATH = "/sparrow-metadata";
    public static final String BASE_PROVIDERS_PATH = "/providers";
    public static final String BASE_CLIENTS_PATH = "/clients";
    public static final String SPLIT = "/";

    public static String getBaseProvidersPath() {
        return BASE_PATH + BASE_PROVIDERS_PATH;
    }

    public static String getBaseClientsPath() {
        return BASE_PATH + BASE_CLIENTS_PATH;
    }

    public static String getProviderNodePath(String ...args) {
        StringBuilder path = new StringBuilder(getBaseProvidersPath());
        for (String arg : args) {
            path.append(SPLIT).append(arg);
        }
        return path.toString();
    }

    public static String getProviderNodePath(String serviceName) {
        return getBaseProvidersPath() + SPLIT + serviceName;
    }

    public static String getPath(String parentPath, String path) {
        return parentPath + SPLIT + path;
    }
}
