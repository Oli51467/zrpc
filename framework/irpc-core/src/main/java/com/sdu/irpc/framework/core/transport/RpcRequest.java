package com.sdu.irpc.framework.core.transport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcRequest {

    /**
     * 请求的id
     */
    private Long requestId;

    /**
     * 请求类型
     */
    private Byte requestType;

    /**
     * 压缩类型
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
     * 具体的消息体
     */
    private RequestPayload requestPayload;
}
