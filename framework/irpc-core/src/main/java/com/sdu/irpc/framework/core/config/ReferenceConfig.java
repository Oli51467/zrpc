package com.sdu.irpc.framework.core.config;

import com.sdu.irpc.framework.core.registration.Registry;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ReferenceConfig<T> {

    private Class<T> targetInterface;

    public Registry registry;

    public Class<T> getInterface() {
        return targetInterface;
    }

    public void setInterface(Class<T> targetInterface) {
        this.targetInterface = targetInterface;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public T get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<T>[] classes = new Class[]{targetInterface};

        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                log.info("method -> {}", method.getName());
                log.info("args -> {}", args);

                // 寻找该服务的可用节点
                List<InetSocketAddress> socketAddress = registry.discover(targetInterface.getName());
                log.info("发现服务【{}】的提供者: {}", targetInterface.getName(), socketAddress.get(0));
                return null;
            }
        });
        return (T) helloProxy;
    }
}
