package com.sdu.irpc.framework.core.proxy;

import com.sdu.irpc.framework.common.enums.SerializationType;
import com.sdu.irpc.framework.core.config.IRpcBootstrap;
import com.sdu.irpc.framework.core.config.ReferenceConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyFactory {

    private static final Map<Class<?>, Object> cache = new ConcurrentHashMap<>(32);

    public static <T> T getProxy(Class<T> clazz) {
        Object bean = cache.get(clazz);
        if (null != bean) {
            return (T) bean;
        }

        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setTargetInterface(clazz);
        reference.setAppName("p1");
        reference.setPath("greet.echo");

        IRpcBootstrap.getInstance().serialize(SerializationType.HESSIAN);
        T t = reference.get();
        cache.put(clazz, t);
        return t;
    }
}
