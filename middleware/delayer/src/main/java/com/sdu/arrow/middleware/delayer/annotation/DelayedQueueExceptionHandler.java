package com.sdu.arrow.middleware.delayer.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
@Component
@Autowired(required = false)
@Qualifier("delayedListener")
public @interface DelayedQueueExceptionHandler {

    String value();

    /**
     * 默认组机制
     */
    String group() default "DEFAULT_GROUP";

}
