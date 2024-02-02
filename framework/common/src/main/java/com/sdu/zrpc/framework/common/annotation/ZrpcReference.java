package com.sdu.zrpc.framework.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ZrpcReference {

    String application() default "default";

    int waitTime() default 5;

    TimeUnit waitTimeUnit() default TimeUnit.SECONDS;
}