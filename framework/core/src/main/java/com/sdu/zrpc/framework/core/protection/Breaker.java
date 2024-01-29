package com.sdu.zrpc.framework.core.protection;

public interface Breaker {

    void recordSuccessRequest();

    void recordErrorRequest();

    void reset();

    void attempt();
}
