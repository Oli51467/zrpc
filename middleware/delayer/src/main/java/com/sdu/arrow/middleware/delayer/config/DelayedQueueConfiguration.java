package com.sdu.arrow.middleware.delayer.config;

import com.sdu.arrow.middleware.delayer.context.DelayedRedisClientSupport;
import com.sdu.arrow.middleware.delayer.context.DelayedThreadPoolExecutor;
import com.sdu.arrow.middleware.delayer.context.DelayedThreadPoolSupport;
import com.sdu.arrow.middleware.delayer.laucher.DelayedBootstrapInitializer;
import com.sdu.arrow.middleware.delayer.redis.DelayedRedissonClientTool;
import com.sdu.arrow.middleware.delayer.redis.RedissonClientTool;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@ComponentScan(basePackages = "com.sdu.arrow.middleware.delayer")
public class DelayedQueueConfiguration {


    /**
     * redisson客户端的实现
     */
    @Bean
    public DelayedRedissonClientTool delayedRedissonClientTool() {
        return new DelayedRedissonClientTool();
    }


    /**
     * 执行操作处理机制（考虑是IO密集型或者混合密集型机制） - 循环监控线程机制
     */
    @Bean("delayedExecuteThreadPoolExecutor")
    public Executor delayedExecuteThreadPoolExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor =
                DelayedThreadPoolExecutor.initParameter("delayedExecuteThreadPoolExecutor");
        // 因为可以定制化线程数量机制，是否考虑延迟机制，待议 TODO
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }


    /**
     * 执行操作处理机制（考虑是IO密集型或者混合密集型机制） 异步 执行线程机制
     */
    @Bean("delayedExecuteThreadPoolCycle")
    public Executor delayedExecuteThreadPoolCycle() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor =
                DelayedThreadPoolExecutor.initParameter("delayedExecuteThreadPoolCycle");
        threadPoolTaskExecutor.setQueueCapacity(0);
        // 系统暂时仅仅支持核心书个组，直接执行，不会存放队列数据信息
        threadPoolTaskExecutor.setMaxPoolSize(threadPoolTaskExecutor.getMaxPoolSize());
        threadPoolTaskExecutor.setCorePoolSize(threadPoolTaskExecutor.getCorePoolSize());
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }


    /**
     * 延迟线程池支持机制
     */
    @Bean
    public DelayedThreadPoolSupport delayedThreadPoolSupport(@Autowired @Qualifier("delayedExecuteThreadPoolExecutor") Executor execute,
                                                             @Autowired @Qualifier("delayedExecuteThreadPoolCycle") Executor recycle) {
        return new DelayedThreadPoolSupport(execute, recycle);
    }


    /**
     * 延迟队列机制支持Redis客户端
     */
    @Bean
    public DelayedRedisClientSupport delayedRedisClientSupport() {
        return new DelayedRedisClientSupport(delayedRedissonClientTool());
    }


    /**
     * 线程池的构建和初始化
     */
    @Bean(initMethod = "init")
    public DelayedBootstrapInitializer delayedThreadPoolExecutor() {
        return new DelayedBootstrapInitializer();
    }


    @Bean
    @ConditionalOnMissingBean(RedissonClientTool.class)
    public RedissonClientTool redissonClientTool(RedissonClient redissonClient) {
        return new RedissonClientTool(redissonClient);
    }
}
