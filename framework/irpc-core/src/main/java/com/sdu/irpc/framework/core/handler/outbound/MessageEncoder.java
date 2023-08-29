package com.sdu.irpc.framework.core.handler.outbound;

import com.sdu.irpc.framework.common.constant.MessageConstant;
import com.sdu.irpc.framework.core.serialization.SerializationFactory;
import com.sdu.irpc.framework.core.serialization.Serializer;
import com.sdu.irpc.framework.core.transport.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 出站时经过的第一个编码处理器
 * 协议编码器
 * 0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22
 * +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 * |    magic          |ver |head  len|    full length    | qt | ser|comp|              RequestId                |
 * +-----+-----+-------+----+----+----+----+-----------+----- ---+--------+----+----+----+----+----+----+---+---+
 * |                                                                                                             |
 * |                                         body                                                                |
 * |                                                                                                             |
 * +--------------------------------------------------------------------------------------------------------+---+
 * 4B magic(魔数)   ---> Irpc.getBytes()
 * 1B version(版本) ---> 1
 * 2B header length 头部的长度
 * 4B total  length 报文总长度
 * 1B serialize 序列化方式
 * 1B compression 压缩方式
 * 1B requestType 请求类型
 * 8B requestId 请求Id
 */
public class MessageEncoder extends MessageToByteEncoder<RpcRequest> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest, ByteBuf byteBuf) throws Exception {
        // 4个字节的魔数值
        byteBuf.writeBytes(MessageConstant.MAGIC);
        // 1个字节的版本号
        byteBuf.writeByte(MessageConstant.VERSION);
        // 2个字节的头部的长度
        byteBuf.writeShort(MessageConstant.HEADER_LENGTH);
        // 请求类型、序列化、压缩类型
        byteBuf.writeByte(rpcRequest.getRequestType());
        byteBuf.writeByte(rpcRequest.getSerializationType());
        byteBuf.writeByte(rpcRequest.getCompressionType());
        byteBuf.writeLong(rpcRequest.getRequestId());
        byteBuf.writeLong(rpcRequest.getTimeStamp());

        // body
        byte[] body = null;
        if (null != rpcRequest.getRequestPayload()) {
            Serializer serializer = SerializationFactory.getSerializer(rpcRequest.getSerializationType()).getImpl();
            body = serializer.serialize(rpcRequest.getRequestPayload());
        }
        if (null != body) {
            byteBuf.writeBytes(body);
        }
    }
}
