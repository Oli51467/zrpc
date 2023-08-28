package com.sdu.irpc.framework.core.config;

public class ServiceConfig<T> {

    private Class<T> rawInterface;
    private Object reference;

    public Class<T> getInterface() {
        return rawInterface;
    }

    public void setInterface(Class<T> rawInterface) {
        this.rawInterface = rawInterface;
    }

    public Object getReference() {
        return reference;
    }

    public void setReference(Object reference) {
        this.reference = reference;
    }
}
