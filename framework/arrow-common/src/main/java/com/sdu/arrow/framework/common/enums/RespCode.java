package com.sdu.arrow.framework.common.enums;

import lombok.Getter;

@Getter
public enum RespCode {

    SUCCESS((byte) 20, "成功"),
    RATE_LIMIT((byte) 31, "服务被限流"),
    RESOURCE_NOT_FOUND((byte) 44, "请求的资源不存在"),
    FAIL((byte) 50, "调用方法发生异常"),
    CLOSING((byte) 51, "服务提供方正在关闭");

    private final byte code;
    private final String desc;

    RespCode(byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
