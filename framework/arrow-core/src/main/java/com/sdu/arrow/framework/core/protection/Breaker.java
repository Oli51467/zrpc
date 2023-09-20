package com.sdu.arrow.framework.core.protection;

public interface Breaker {

    void recordSuccessRequest();

    void recordErrorRequest();

    void reset();

    void attempt();
}
