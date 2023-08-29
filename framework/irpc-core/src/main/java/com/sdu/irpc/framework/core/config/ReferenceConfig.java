package com.sdu.irpc.framework.core.config;

import com.sdu.irpc.framework.core.proxy.RpcClientInvocationHandler;
import com.sdu.irpc.framework.core.registration.Registry;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

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
        InvocationHandler handler = new RpcClientInvocationHandler(registry, targetInterface);

        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, handler);
        return (T) helloProxy;
    }
}
