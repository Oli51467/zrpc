package com.sdu.zrpc.framework.core.proxy;

import com.sdu.zrpc.framework.core.config.ReferenceConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ProxyFactory {

    private static final Map<Class<?>, Object> cache = new ConcurrentHashMap<>(32);

    public static <T> T getProxy(Class<T> clazz, String application, Integer waitTime, TimeUnit waitTimeUnit) {
        if (clazz == null) {
            throw new IllegalArgumentException("Target class cannot be null.");
        }
        long waitTimeMillis = waitTimeUnit.toMillis(waitTime);
        if (waitTimeMillis <= 0 || waitTimeMillis > 5000) {
            waitTimeMillis = 5000;
        }

        Object bean = cache.get(clazz);
        if (null != bean) {
            return clazz.cast(bean);
        }

        ReferenceConfig<T> reference = new ReferenceConfig<>(clazz, application, waitTimeMillis);

        try {
            T t = reference.get();
            cache.put(clazz, t);
            return t;
        } catch (Exception e) {
            log.error("Failed to create proxy for class: " + clazz.getName(), e);
            throw new IllegalStateException("Failed to create proxy for class: " + clazz.getName(), e);
        }
    }
}
