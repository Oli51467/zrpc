package com.sdu.arrow.middleware.ratelimiter.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rate-limiter")
public class RateLimiterController {

    @Autowired
    private SampleExecuteRateLimiter executeRateLimiter;


    @GetMapping("/test")
    public ResponseResult create(@RequestParam("processTime") int processTime,
                                 @RequestParam("threadCount") int threadCount) {
        try {
            for (int idx = 0; idx < threadCount; idx++) {
                new Thread(() -> {
                    try {
                        executeRateLimiter.executeRateLimiter(processTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            return ResponseResult.ok();
        } catch (Exception e) {
            return ResponseResult.fail("create rateLimiter is failure!");
        }
    }
}
