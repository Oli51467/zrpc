package com.sdu.arrow.middleware.delayer.annotation;

import com.sdu.arrow.middleware.delayer.config.DelayedQueueConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
@SpringBootConfiguration
@Import(DelayedQueueConfiguration.class)
public @interface EnableDelayedQueue {
}
