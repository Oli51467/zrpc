package com.sdu.zrpc.framework.core.config;

import com.sdu.zrpc.framework.core.proxy.RpcClientInvocationHandler;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

@Slf4j
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceConfig<T> {

    private Class<T> targetInterface;
    private String appName = "default";
    private Long waitTimeMillis;

    public T get() {
        if (targetInterface == null) {
            throw new IllegalArgumentException("Target interface cannot be null.");
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?>[] classes = new Class[]{targetInterface};
        // InvocationHandler接口是proxy代理实例的调用处理程序实现的一个接口，
        // 每一个proxy代理实例都有一个关联的调用处理程序；在代理实例调用方法时，方法调用被编码分派到调用处理程序的invoke方法。
        InvocationHandler handler = new RpcClientInvocationHandler(appName, waitTimeMillis);
        Object remoteProcessCallProxy = Proxy.newProxyInstance(classLoader, classes, handler);
        if (targetInterface.isInstance(remoteProcessCallProxy)) {
            return targetInterface.cast(remoteProcessCallProxy);
        } else {
            throw new IllegalStateException("Proxy does not implement the target interface.");
        }
    }
}
