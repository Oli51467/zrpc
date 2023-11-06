package com.sdu.arrow.middleware.delayer.web;

import com.sdu.arrow.middleware.common.response.ResponseResult;
import com.sdu.arrow.middleware.delayer.model.ExecuteInvokerEvent;
import com.sdu.arrow.middleware.delayer.redis.DelayedRedissonClientTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/delayed")
@RestController
public class DelayerQueueController {

    @Autowired
    DelayedRedissonClientTool delayedRedissionClientTool;

    /**
     * 创建对应的延时队列对象服务
     */
    @PostMapping("/publish")
    public ResponseResult publish(@RequestBody ExecuteInvokerEvent executeInvokerEvent) {
        try {
            delayedRedissionClientTool.offer(executeInvokerEvent);
            return ResponseResult.ok(executeInvokerEvent);
        } catch (Exception e) {
            log.error("create delaye element is failure!", e);
            return ResponseResult.fail("create delaye element is failure!");
        }
    }

}
