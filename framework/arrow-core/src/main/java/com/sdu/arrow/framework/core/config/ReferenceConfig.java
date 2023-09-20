package com.sdu.arrow.framework.core.config;

import com.sdu.arrow.framework.core.proxy.RpcClientInvocationHandler;
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
    private String path;
    private String appName;

    public T get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?>[] classes = new Class[]{targetInterface};
        InvocationHandler handler = new RpcClientInvocationHandler(appName, path);

        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, handler);
        return (T) helloProxy;
    }
}
