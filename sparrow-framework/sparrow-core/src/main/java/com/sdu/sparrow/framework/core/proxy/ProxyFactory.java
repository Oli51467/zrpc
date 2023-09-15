package com.sdu.sparrow.framework.core.proxy;

import com.sdu.sparrow.framework.common.enums.SerializationType;
import com.sdu.sparrow.framework.core.config.RpcBootstrap;
import com.sdu.sparrow.framework.core.config.ReferenceConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyFactory {

    private static final Map<Class<?>, Object> cache = new ConcurrentHashMap<>(32);

    public static <T> T getProxy(Class<T> clazz, String application, String path) {
        Object bean = cache.get(clazz);
        if (null != bean) {
            return (T) bean;
        }

        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setTargetInterface(clazz);
        reference.setAppName(application);
        reference.setPath(path);

        RpcBootstrap.getInstance().serialize(SerializationType.HESSIAN);
        T t = reference.get();
        cache.put(clazz, t);
        return t;
    }
}