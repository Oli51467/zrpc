package com.sdu.arrow.framework.core.config;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;

@Setter
@Getter
public class ServiceConfig {

    private Method method;
    private Object reference;
    private String applicationName;
    private String path;
}
