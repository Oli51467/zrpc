package com.sdu.irpc.framework.common.entity.rpc;

public class ServiceConfig {

    private Class<?> rawInterface;
    private Object reference;
    private String applicationName;

    public Class<?> getInterface() {
        return rawInterface;
    }

    public void setInterface(Class<?> rawInterface) {
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
