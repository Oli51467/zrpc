package com.sdu.arrow.middleware.ratelimiter.sample;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResponseCode {

    /**
     * 成功返回的状态码
     */
    SUCCESS(200, "success"),

    FAIL(400, "fail"),
    ;

    private int code;
    private String msg;
}
