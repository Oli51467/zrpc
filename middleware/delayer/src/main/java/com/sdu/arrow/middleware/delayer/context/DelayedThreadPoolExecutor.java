package com.sdu.arrow.middleware.delayer.context;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class DelayedThreadPoolExecutor {


    /**
     * 获取到服务器的cpu内核：逻辑内核核心数
     */
    private static int DEFAULT_THREAD_CORE_BASE_SIZE = Runtime.getRuntime().availableProcessors();

    /**
     * IO密集型机制控制*2
     */
    private static int DEFAULT_THREAD_CORE_SIZE_IO_TYPE = DEFAULT_THREAD_CORE_BASE_SIZE << 1;


    /**
     * 序号分配器
     */
    private static AtomicInteger atomicInteger = new AtomicInteger();


    /**
     * 初始化参数信息
     */
    public static ThreadPoolTaskExecutor initParameter(String threadGroup) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(DEFAULT_THREAD_CORE_SIZE_IO_TYPE);//核心池大小
        executor.setMaxPoolSize(DEFAULT_THREAD_CORE_SIZE_IO_TYPE << 4);//最大线程数 = 核心*核心池大小;
        executor.setQueueCapacity(1000);//队列程度
        executor.setKeepAliveSeconds(30);//线程空闲时间
        executor.setThreadGroupName(threadGroup);
        executor.setThreadFactory(r -> new Thread(r, String.format("%s-%s", threadGroup, atomicInteger.getAndDecrement())));
        executor.setThreadNamePrefix(threadGroup + "-");//线程前缀名称
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());//配置拒绝策略
        return executor;
    }


}
