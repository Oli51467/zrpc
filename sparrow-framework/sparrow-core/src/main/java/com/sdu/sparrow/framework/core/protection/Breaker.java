package com.sdu.sparrow.framework.core.protection;

public interface Breaker {

    void recordSuccessRequest();

    void recordErrorRequest();

    void reset();

    void attempt();
}
