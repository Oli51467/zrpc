package com.sdu.irpc.framework.core.handler.inbound;

import com.sdu.irpc.framework.common.constant.MessageConstant;
import com.sdu.irpc.framework.common.enums.RequestType;
import com.sdu.irpc.framework.common.exception.ProtocolException;
import com.sdu.irpc.framework.core.compression.Compressor;
import com.sdu.irpc.framework.core.compression.CompressorFactory;
import com.sdu.irpc.framework.core.serialization.SerializationFactory;
import com.sdu.irpc.framework.core.serialization.Serializer;
import com.sdu.irpc.framework.core.transport.RequestPayload;
import com.sdu.irpc.framework.core.transport.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestMessageDecoder extends LengthFieldBasedFrameDecoder implements MessageConstant {

    /**
     * maxFrameLength：本次能接收的最大的数据长度
     * lengthFieldOffset：设置的长度域的偏移量，长度域在数据包的起始位置
     * lengthFieldLength：长度域的长度
     * lengthAdjustment：数据包的偏移量，计算方式 = 数据长度 + lengthAdjustment = 数据总长度
     * initialBytesToStrip：需要跳过的字节数
     */
    public RequestMessageDecoder() {
        super(MAX_FRAME_LENGTH,
                MAGIC.length + VERSION_LENGTH + HEADER_FIELD_LENGTH,
                FULL_FIELD_LENGTH,
                -(MAGIC.length + VERSION_LENGTH + HEADER_FIELD_LENGTH + FULL_FIELD_LENGTH),
                0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        /*
         * super方法已经将二进制流读成ByteBuf
         * 先根据提供的长度偏移和长度域长度读取数据包的长度
         * 根据数据包的长度+偏移量长度计算出本次需要读取的总数据包的长度
         * 根据设置的跳过数据的长度计算有效的护数据长度
         * 将有效的数据长度读取出来，然后改变读指针的位置到数据包的末尾
         * 将解码出来的数据交给ByteToMessageDecoder进行后续操作
         */
        Object decodeResult = super.decode(ctx, in);
        if (decodeResult instanceof ByteBuf byteBuf) {
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
        byte requestType = byteBuf.readByte();
        // 序列化类型
        byte serializationType = byteBuf.readByte();
        // 压缩类型
        byte compressionType = byteBuf.readByte();
        // 请求id
        long requestId = byteBuf.readLong();
        // 时间戳
        long timeStamp = byteBuf.readLong();
        RpcRequest request = new RpcRequest();
        request.setRequestId(requestId);
        request.setRequestType(requestType);
        request.setCompressionType(compressionType);
        request.setSerializationType(serializationType);
        request.setTimeStamp(timeStamp);
        // 心跳请求不处理
        if (requestType == RequestType.HEART_BEAT.getCode()) {
            return request;
        }
        int payloadLength = fullLength - headerLength;
        byte[] payload = new byte[payloadLength];
        byteBuf.readBytes(payload);

        if (payload.length != 0) {
            // 解压缩payload
            Compressor compressor = CompressorFactory.getCompressor(compressionType).getImpl();
            payload = compressor.decompress(payload);
            // 反序列化
            Serializer serializer = SerializationFactory.getSerializer(serializationType).getImpl();
            RequestPayload requestPayload = serializer.deserialize(payload, RequestPayload.class);
            request.setRequestPayload(requestPayload);
        }
        log.debug("请求【{}】已经在服务端完成解码工作。", request.getRequestId());
        return request;
    }

}
