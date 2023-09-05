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
    private String path;
    private String appName;

    public T get() {
        // 开启心跳探活
        HeartbeatDetector heartbeatDetector = new HeartbeatDetector(appName, path);
        heartbeatDetector.startDetect();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?>[] classes = new Class[]{targetInterface};
        InvocationHandler handler = new RpcClientInvocationHandler(appName, path);

        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, handler);
        return (T) helloProxy;
    }
}
