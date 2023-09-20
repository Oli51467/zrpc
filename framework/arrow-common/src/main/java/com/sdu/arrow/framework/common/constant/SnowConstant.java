package com.sdu.arrow.framework.common.constant;

public interface SnowConstant {

    Long DATA_CENTER_BIT = 5L;
    Long MACHINE_BIT = 5L;
    Long SEQUENCE_BIT = 12L;

    Long DATA_CENTER_MAX = ~(-1L << DATA_CENTER_BIT);
    Long MACHINE_MAX = ~(-1L << MACHINE_BIT);
    Long SEQUENCE_MAX = ~(-1L << SEQUENCE_BIT);

    Long TIMESTAMP_LEFT = DATA_CENTER_BIT + MACHINE_BIT + SEQUENCE_BIT;
    Long DATA_CENTER_LEFT = MACHINE_BIT + SEQUENCE_BIT;
    Long MACHINE_LEFT = SEQUENCE_BIT;
}
