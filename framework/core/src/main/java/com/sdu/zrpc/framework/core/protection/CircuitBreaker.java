package com.sdu.zrpc.framework.core.protection;

import com.sdu.zrpc.framework.common.enums.CircuitStatusEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CircuitBreaker implements Breaker {

    // 熔断器状态
    public static volatile CircuitStatusEnum status = CircuitStatusEnum.CLOSE;
    // 异常的请求数
    private final AtomicInteger errorRequestCount = new AtomicInteger(0);
    // 记录是否在半开期，使用ThreadLocal来存储线程状态
    private final ThreadLocal<Boolean> attemptLocal = ThreadLocal.withInitial(() -> false);
    // 异常的阈值
    private final int maxErrorCount = 10;
    // 打开状态持续时间，单位毫秒
    private static final long OPEN_DURATION = 50;
    // 记录熔断器打开的时间
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
            status = CircuitStatusEnum.OPEN;
            openTime = System.currentTimeMillis();
            log.info("重试仍失败，熔断器重新打开");
        } else {
            // 普通失败，记录失败次数。判断是否需要打开
            errorRequestCount.incrementAndGet();
            if (status != CircuitStatusEnum.OPEN && errorRequestCount.get() >= maxErrorCount) {
                status = CircuitStatusEnum.OPEN;
                openTime = System.currentTimeMillis();
                log.info("失败次数过多，熔断器打开");
            }
        }
    }

    /**
     * 重置熔断器
     */
    public void reset() {
        status = CircuitStatusEnum.CLOSE;
        errorRequestCount.set(0);
    }

    public synchronized void attempt() {
        if (status == CircuitStatusEnum.CLOSE) {
            return;
        }
        if (status == CircuitStatusEnum.HALF_OPEN) {
            throw new RuntimeException("断路器开启，无法发送请求");
        }
        if (status == CircuitStatusEnum.OPEN) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - openTime >= OPEN_DURATION) {
                status = CircuitStatusEnum.HALF_OPEN;
                attemptLocal.set(true);
                log.info("熔断器设置为半打开状态");
            } else {
                log.info("请求被熔断");
                throw new RuntimeException("断路器开启，无法发送请求");
            }
        }
    }
}
