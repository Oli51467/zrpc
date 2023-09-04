package com.sdu.irpc.framework.core.config;

import com.sdu.irpc.framework.common.constant.RpcMessageConstant;
import com.sdu.irpc.framework.common.constant.ZooKeeperConstant;

public class ServiceConfig {

    private Class<?> rawInterface;
    private Object reference;
    private String applicationName;
    private String group = "default";

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

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }
}
