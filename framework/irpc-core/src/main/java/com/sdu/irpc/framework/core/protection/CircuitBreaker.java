package com.sdu.irpc.framework.core.protection;

import com.sdu.irpc.framework.common.enums.CircuitStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CircuitBreaker implements Breaker {

    // 熔断器状态
    public static volatile CircuitStatus status = CircuitStatus.CLOSE;
    // 异常的请求数
    private final AtomicInteger errorRequestCount = new AtomicInteger(0);
    // 记录是否在半开期，使用ThreadLocal来存储线程状态
    private final ThreadLocal<Boolean> attemptLocal = ThreadLocal.withInitial(() -> false);
    // 异常的阈值
    private final int maxErrorCount = 10;
    // 打开状态持续时间，单位毫秒
    private static final long OPEN_DURATION = 50;
    // 记录熔断器打开的实际爱你
    private long openTime = 0;

    // 每次发生请求，获取发生异常应该进行记录
    public void recordSuccessRequest() {
        if (attemptLocal.get()) {
            attemptLocal.remove();
            reset();
        }
    }

    public void recordErrorRequest() {
        // 说明当前线程进入了半打开状态的熔断器，且执行失败。重新打开熔断器
        if (attemptLocal.get()) {
            attemptLocal.remove();
            status = CircuitStatus.OPEN;
            openTime = System.currentTimeMillis();
            log.info("重试仍失败，熔断器重新打开");
        } else {
            // 普通失败，记录失败次数。判断是否需要打开
            errorRequestCount.incrementAndGet();
            if (status != CircuitStatus.OPEN && errorRequestCount.get() >= maxErrorCount) {
                status = CircuitStatus.OPEN;
                openTime = System.currentTimeMillis();
                log.info("失败次数过多，熔断器打开");
            }
        }
    }

    /**
     * 重置熔断器
     */
    public void reset() {
        status = CircuitStatus.CLOSE;
        errorRequestCount.set(0);
    }

    public synchronized boolean attempt() {
        if (status == CircuitStatus.CLOSE) {
            return true;
        }
        if (status == CircuitStatus.HALF_OPEN) {
            log.info("当前状态为半打开，已经有线程进入");
            return false;
        }
        if (status == CircuitStatus.OPEN) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - openTime >= OPEN_DURATION) {
                status = CircuitStatus.HALF_OPEN;
                attemptLocal.set(true);
                log.info("设置为半打开状态");
                return true;
            } else {
                log.info("请求被熔断");
                return false;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        CircuitBreaker circuitBreaker = new CircuitBreaker();
        for (int i = 0; i < 1000; i ++ ) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Random random = new Random();
                    try {
                        Thread.sleep(random.nextInt(500));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    int a = random.nextInt(1000);
                    if (circuitBreaker.attempt()) {
                        if (a <= 100) {
                            circuitBreaker.recordErrorRequest();
                            System.out.println("Error");
                        } else {
                            circuitBreaker.recordSuccessRequest();
                            System.out.println("Success");
                        }
                    } else {
                        System.out.println("Failed");
                    }
                }
            });
            thread.start();
        }
    }
}
