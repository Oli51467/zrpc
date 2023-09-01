package com.sdu.irpc.framework.common.entity.rpc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务提供方回复的响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcResponse {

    /**
     * 请求的id
     */
    private Long requestId;

    /**
     * 压缩方式
     */
    private Byte compressionType;

    /**
     * 序列化方式
     */
    private Byte serializationType;

    /**
     * 时间戳
     */
    private Long timeStamp;

    /**
     * 响应码
     * 1 成功
     * 2 异常
     */
    private Byte code;

    /**
     * 具体的消息体
     */
    private Object body;
}
