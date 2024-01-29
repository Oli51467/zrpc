package com.sdu.zrpc.framework.common.util;

import com.sdu.zrpc.framework.common.constant.SnowConstant;

import java.util.concurrent.atomic.LongAdder;

public class IdGenerator implements SnowConstant {

    public static final long START_STAMP = DateUtil.get("2022-1-1").getTime();

    private final Long dataCenterId;
    private final Long machineId;
    private Long lastTimeStamp = -1L;
    private final LongAdder sequenceId = new LongAdder();

    public IdGenerator(long dataCenterId, long machineId) {
        // 判断参数是否合法
        if (dataCenterId > DATA_CENTER_MAX || machineId > MACHINE_MAX) {
            throw new IllegalArgumentException("你传入的数据中心编号或机器号不合法.");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    public Long getId() {
        long currentTime = System.currentTimeMillis();

        long timeStamp = currentTime - START_STAMP;

        // 判断时钟回拨
        if (timeStamp < lastTimeStamp) {
            throw new RuntimeException("您的服务器进行了时钟回调.");
        }
        if (timeStamp == lastTimeStamp) {
            sequenceId.increment();
            if (sequenceId.sum() >= SEQUENCE_MAX) {
                timeStamp = nextTimeStamp();
                sequenceId.reset();
            }
        } else {
            sequenceId.reset();
        }
        long sequence = sequenceId.sum();
        lastTimeStamp = timeStamp;
        return timeStamp << TIMESTAMP_LEFT | dataCenterId << DATA_CENTER_LEFT
                | machineId << MACHINE_LEFT | sequence;
    }

    private Long nextTimeStamp() {
        long current = System.currentTimeMillis() - START_STAMP;
        while (current == lastTimeStamp) {
            current = System.currentTimeMillis() - START_STAMP;
        }
        return current;
    }

    public static void main(String[] args) {
        IdGenerator idGenerator = new IdGenerator(1, 2);
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> System.out.println(idGenerator.getId())).start();
        }
    }
}
