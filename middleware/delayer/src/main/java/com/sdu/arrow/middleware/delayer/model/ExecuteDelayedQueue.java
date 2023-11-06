package com.sdu.arrow.middleware.delayer.model;

import lombok.Data;


@Data
public class ExecuteDelayedQueue {

    private String queueName;

    private String queueGroup;

}
