package com.sdu.irpc.framework.core.config;

public class ServiceConfig<T> {

    private Class<T> rawInterface;
    private Object reference;
    private String applicationName;

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

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}
