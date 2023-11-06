package com.sdu.arrow.middleware.delayer;

import org.redisson.api.CronSchedule;
import org.redisson.api.RScheduledExecutorService;
import org.redisson.api.RScheduledFuture;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

@Component
public class SchedulerController {

    @Autowired
    RedissonClient redissonClient;


    public void publicSchedulerTask() {
        RScheduledExecutorService scheduledExecutorService = redissonClient.getExecutorService("taskScheduler");
        RScheduledFuture<?> rScheduledFuture = scheduledExecutorService.scheduleAsync(new Task(), CronSchedule.of("* * * * * ?"));
        try {
            System.out.println(rScheduledFuture.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static class Task implements Runnable, Serializable {
        @Override
        public void run() {
            System.out.println("execute task :{}" + System.currentTimeMillis());
        }
    }
}
