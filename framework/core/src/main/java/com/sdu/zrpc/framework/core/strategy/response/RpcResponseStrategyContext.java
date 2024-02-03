package com.sdu.zrpc.framework.core.strategy.response;

import com.sdu.zrpc.framework.common.enums.RespCode;

import java.util.HashMap;
import java.util.Map;

public class RpcResponseStrategyContext {

    private static final Map<Byte, RpcResponseStrategy> responseStrategyMap = new HashMap<>();

    static {
        registerResponseStrategy(RespCode.SUCCESS.getCode(), new SuccessResponseStrategy());
        registerResponseStrategy(RespCode.RESOURCE_NOT_FOUND.getCode(), new ResourceNotFoundResponseStrategy());
        registerResponseStrategy(RespCode.FAIL.getCode(), new ErrorResponseStrategy());
        registerResponseStrategy(RespCode.CLOSING.getCode(), new ServiceClosingResponseStrategy());
        registerResponseStrategy(RespCode.HEARTBEAT.getCode(), new HeartBeatResponseStrategy());
        registerResponseStrategy(RespCode.RATE_LIMIT.getCode(), new RateLimitResponseStrategy());
    }

    // 注册策略
    public static void registerResponseStrategy(Byte responseCode, RpcResponseStrategy strategy) {
        responseStrategyMap.put(responseCode, strategy);
    }
    // 获取策略
    public static RpcResponseStrategy getResponseStrategy(Byte responseCode) {
        return responseStrategyMap.get(responseCode);
    }
}
