package com.sdu.zrpc.framework.common.response.exception;

import lombok.Getter;

@Getter
public enum ExceptionEnum {

    LOCK_LIMIT("请求太频繁"),
    REPEAT_REQUEST("重复请求"),
    FREQUENCY_LIMIT("请求过于频繁"),
    NOT_AUTH("没有权限"),
    ;

    private final String msg;

    ExceptionEnum(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "ExceptionEnum{" +
                "msg='" + msg + '\'' +
                '}';
    }
}
