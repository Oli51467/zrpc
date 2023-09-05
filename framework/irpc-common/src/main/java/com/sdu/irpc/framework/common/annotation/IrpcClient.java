package com.sdu.irpc.framework.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IrpcClient {

    String application() default "default";

    String path() default "";

    int retry() default 0;

    int interval() default 2000;
}
