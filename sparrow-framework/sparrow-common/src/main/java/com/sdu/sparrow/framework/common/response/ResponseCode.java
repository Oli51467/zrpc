package com.sdu.sparrow.framework.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResponseCode {

    /**
     * 返回的状态码
     */
    SUCCESS(200, "成功"),

    FAIL(400, "失败"),
    ;

    private final int code;
    private final String msg;
}
