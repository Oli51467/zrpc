package com.sdu.zrpc.framework.core.proxy;

import com.sdu.zrpc.framework.core.config.ReferenceConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyFactory {

    private static final Map<Class<?>, Object> cache = new ConcurrentHashMap<>(32);

    public static <T> T getProxy(Class<T> clazz, String application) {
        Object bean = cache.get(clazz);
        if (null != bean) {
            return (T) bean;
        }

        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setTargetInterface(clazz);
        reference.setAppName(application);

        T t = reference.get();
        cache.put(clazz, t);
        return t;
    }
}
