package com.sdu.arrow.middleware.delayer.laucher;

import com.sdu.arrow.middleware.delayer.annotation.DelayedQueueExceptionHandler;
import com.sdu.arrow.middleware.delayer.annotation.DelayedQueueListener;
import com.sdu.arrow.middleware.delayer.context.DelayedBootstrapRunnable;
import com.sdu.arrow.middleware.delayer.context.DelayedThreadPoolSupport;
import com.sdu.arrow.middleware.delayer.listener.DelayedExceptionHandler;
import com.sdu.arrow.middleware.delayer.listener.EventExecutableInvokerListener;
import com.sdu.arrow.middleware.delayer.listener.ExecutableExceptionHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
public class DelayedBootstrapInitializer {


    @Setter
    @Getter
    @DelayedQueueListener(value = "delayedListenerContextMap")
    Map<String, EventExecutableInvokerListener> delayedListenerContextMap = new HashMap<>();


    @Setter
    @Getter
    @DelayedQueueExceptionHandler(value = "delayedExceptionHandlerMap")
    Map<String, DelayedExceptionHandler> delayedExceptionHandlerMap = new HashMap<>();

    public static String getAnnotationMetadataGroupListener(EventExecutableInvokerListener eventExecutableInvokerListener) {
        return getAnnotationMetadataGroup(eventExecutableInvokerListener, DelayedQueueListener.class);
    }

    public static String getAnnotationMetadataGroupExceptionHandler(DelayedExceptionHandler delayedExceptionHandler) {
        return getAnnotationMetadataGroup(delayedExceptionHandler, DelayedQueueExceptionHandler.class);
    }

    /**
     * 获取相关的组信息
     */
    public static String getAnnotationMetadataGroup(Object object, Class delayedQueueListenerClass) {
        Object annotationInstance = object.getClass().getAnnotation(delayedQueueListenerClass);
        if (annotationInstance instanceof DelayedQueueListener delayedQueueListener) {
            return delayedQueueListener.group();
        } else if (annotationInstance instanceof DelayedQueueExceptionHandler delayedExceptionHandler) {
            return delayedExceptionHandler.group();
        }
        return Strings.EMPTY;
    }

    /**
     * 执行线程组机制
     *
     */
    public static Executor getExecutorByGroup(List<EventExecutableInvokerListener> eventExecutableInvokerListeners) {
        return eventExecutableInvokerListeners.stream().map(EventExecutableInvokerListener::getExecutor).
                filter(Objects::nonNull).findAny().orElse(null);
    }

    /**
     * 初始化操作机制控制
     */
    public void init() {
        log.info("启动初始化加载并完成所有相关延迟启动初始化加载并完成所有相关延迟" +
                "系统中用于侦听上下文接口服务数据的队列 : {}", delayedListenerContextMap);
        log.info("开始完成线程任务分配，并为每个组的侦听器和任务队列分配资源");
        if (MapUtils.isEmpty(delayedListenerContextMap)) {
            log.info("未找到任务侦听信息。在springContext管理的上下文中，" +
                    "请检查是否有关于实现的接口" +
                    "EventExecutableInvokerListener，以及相关@DelayedQueueListener");
            return;
        }
        log.info("启动与生产相关的侦听绑定机制");
        Map<String, List<EventExecutableInvokerListener>> getAnnotationMetadataGroup =
                delayedListenerContextMap.values().stream().collect(Collectors.groupingBy(DelayedBootstrapInitializer::getAnnotationMetadataGroupListener));

        log.info("开始初始化相关的异常信息处理机制");
        Map<String, List<DelayedExceptionHandler>> delayedExceptionHandlerMapGroup =
                delayedExceptionHandlerMap.values().stream().collect(Collectors.groupingBy(DelayedBootstrapInitializer::getAnnotationMetadataGroupExceptionHandler));


        if (MapUtils.isNotEmpty(getAnnotationMetadataGroup)) {
            Executor executor = DelayedThreadPoolSupport.getTaskRecycleThread();
            log.info("启动资源分配机制");
            //推荐同一个组里面采用一个线程池进行处理机制
            getAnnotationMetadataGroup.forEach((key, value) -> {
                log.info("初始化线程机制 {}：", value);
                executor.execute(new DelayedBootstrapRunnable(key, value,
                        DelayedBootstrapInitializer.getExecutorByGroup(value),
                        new ExecutableExceptionHandler(delayedExceptionHandlerMapGroup.get(key))));
            });
        } else {
            log.warn("资源转换失败！无法执行资源执行机制");
        }
    }

}
