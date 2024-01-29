package com.sdu.zrpc.framework.common.enums;

import lombok.Getter;

/**
 * 标记请求类型
 */
@Getter
public enum RequestType {

    REQUEST((byte) 1, "普通请求"),
    HEART_BEAT((byte) 2, "心跳检测请求");

    private final byte code;
    private final String type;

    RequestType(byte code, String type) {
        this.code = code;
        this.type = type;
    }
}
