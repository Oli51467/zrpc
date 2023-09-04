package com.sdu.irpc.framework.core.handler.inbound;

import com.sdu.irpc.framework.common.constant.RpcMessageConstant;
import com.sdu.irpc.framework.common.entity.rpc.RpcResponse;
import com.sdu.irpc.framework.common.exception.ProtocolException;
import com.sdu.irpc.framework.core.compressor.Compressor;
import com.sdu.irpc.framework.core.compressor.CompressorFactory;
import com.sdu.irpc.framework.core.serializer.Serializer;
import com.sdu.irpc.framework.core.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResponseMessageDecoder extends LengthFieldBasedFrameDecoder implements RpcMessageConstant {
    /**
     * maxFrameLength：本次能接收的最大的数据长度
     * lengthFieldOffset：设置的长度域的偏移量，长度域在数据包的起始位置
     * lengthFieldLength：长度域的长度
     * lengthAdjustment：数据包的偏移量，计算方式 = 数据长度 + lengthAdjustment = 数据总长度
     * initialBytesToStrip：需要跳过的字节数
     */
    public ResponseMessageDecoder() {
        super(MAX_FRAME_LENGTH,
                MAGIC.length + VERSION_LENGTH + HEADER_FIELD_LENGTH,
                FULL_FIELD_LENGTH,
                -(MAGIC.length + VERSION_LENGTH + HEADER_FIELD_LENGTH + FULL_FIELD_LENGTH),
                0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if (decode instanceof  ByteBuf byteBuf) {
            return decodeFrame(byteBuf);
        }
        return null;
    }

    private Object decodeFrame(ByteBuf byteBuf) {
        // 解析魔数
        byte[] magic = new byte[MAGIC.length];
        byteBuf.readBytes(magic);
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != MAGIC[i]) {
                throw new ProtocolException("请求非法");
            }
        }
        // 解析版本号
        byte version = byteBuf.readByte();
        if (version > VERSION) {
            throw new ProtocolException("请求版本不支持");
        }
        // 解析头部的长度
        short headerLength = byteBuf.readShort();
        // 解析总长度
        int fullLength = byteBuf.readInt();
        // 请求类型
        byte responseCode = byteBuf.readByte();
        // 序列化类型
        byte serializationType = byteBuf.readByte();
        // 压缩类型
        byte compressionType = byteBuf.readByte();
        // 请求id
        long requestId = byteBuf.readLong();
        // 时间戳
        long timeStamp = byteBuf.readLong();
        RpcResponse response = new RpcResponse();
        response.setRequestId(requestId);
        response.setCode(responseCode);
        response.setSerializationType(serializationType);
        response.setTimeStamp(timeStamp);
        response.setCompressionType(compressionType);

        int responseObjectLength = fullLength - headerLength;
        byte[] responseObject = new byte[responseObjectLength];
        byteBuf.readBytes(responseObject);

        if (responseObject.length > 0) {
            // 解压缩payload
            Compressor compressor = CompressorFactory.getCompressor(compressionType).getImpl();
            responseObject = compressor.decompress(responseObject);
            // 反序列化
            Serializer serializer = SerializerFactory.getSerializer(serializationType).getImpl();
            Object responseBody = serializer.deserialize(responseObject, Object.class);
            response.setBody(responseBody);
        }
        log.debug("请求【{}】已经在客户端端完成解码工作。", response.getRequestId());
        return response;
    }
}
