package com.sdu.arrow.middleware.delayer.redis;

import com.sdu.arrow.middleware.delayer.model.ExecuteInvokerEvent;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;

import java.util.Objects;

@AutoConfigureAfter(value = RedissonClientTool.class)
@Slf4j
public class DelayedRedissonClientTool {

    @Autowired
    private RedissonClientTool redissonClientTool;

    /**
     * 自动注册
     */
    public DelayedRedissonClientTool() {
    }

    /**
     * 手动注册
     *
     */
    public DelayedRedissonClientTool(RedissonClientTool redissonClientTool) {
        this.redissonClientTool = redissonClientTool;
    }

    /**
     * 添加阻塞队列-元素
     */
    public <T> void offer(ExecuteInvokerEvent<T> executeInvokerEvent) {
        //预先进行构建初始化参数条件机制
        executeInvokerEvent.preCondition(executeInvokerEvent);
        redissonClientTool.addDelayQueueElement(Objects.requireNonNull(executeInvokerEvent).getBizGroup(),
                executeInvokerEvent, executeInvokerEvent.getDelayedTime(), executeInvokerEvent.getTimeUnit());
    }


    /**
     * 获取相关的
     *
     */
    public <T> RBlockingQueue<T> takeBlockingQueue(ExecuteInvokerEvent<T> executeInvokerEvent) {
        return redissonClientTool.getRedissonClient().getBlockingQueue(executeInvokerEvent.getBizGroup());

    }

    /**
     * 操作梳理
     *
     */
    public <T> ExecuteInvokerEvent<T> poll(RBlockingQueue<T> trBlockingQueue) throws InterruptedException {
        return (ExecuteInvokerEvent<T>) trBlockingQueue.take();
    }

}
