package com.sdu.irpc.framework.core.config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ReferenceConfig<T> {

    private Class<T> rawInterface;

    public Class<T> getInterface() {
        return rawInterface;
    }

    public void setInterface(Class<T> rawInterface) {
        this.rawInterface = rawInterface;
    }

    public T get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{rawInterface};

        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("Hello Proxy");
                return null;
            }
        });
        return (T) helloProxy;
    }
}
