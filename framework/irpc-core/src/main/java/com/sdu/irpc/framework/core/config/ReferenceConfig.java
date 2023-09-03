package com.sdu.irpc.framework.core.config;

import com.sdu.irpc.framework.core.HeartbeatDetector;
import com.sdu.irpc.framework.core.proxy.RpcClientInvocationHandler;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

@Slf4j
@Setter
public class ReferenceConfig<T> {

    private Class<T> targetInterface;
    private String appName;

    public T get() {
        // 开启心跳探活
        HeartbeatDetector heartbeatDetector = new HeartbeatDetector(appName, targetInterface.getName());
        heartbeatDetector.startDetect();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<T>[] classes = new Class[]{targetInterface};
        InvocationHandler handler = new RpcClientInvocationHandler(appName, targetInterface);

        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, handler);
        return (T) helloProxy;
    }
}
