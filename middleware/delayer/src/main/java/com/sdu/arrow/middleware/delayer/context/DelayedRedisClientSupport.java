package com.sdu.arrow.middleware.delayer.context;

import com.sdu.arrow.middleware.delayer.redis.DelayedRedissonClientTool;
import lombok.Getter;

public class DelayedRedisClientSupport {


    @Getter
    private static DelayedRedissonClientTool delayedRedissonClientTool;

    /**
     * 延迟队列控制redis服务机制
     */
    public DelayedRedisClientSupport(DelayedRedissonClientTool delayedRedissonClientTool) {
        DelayedRedisClientSupport.delayedRedissonClientTool = delayedRedissonClientTool;
    }


}
