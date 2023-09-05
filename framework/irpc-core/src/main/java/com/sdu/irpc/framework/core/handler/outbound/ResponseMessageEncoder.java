package com.sdu.irpc.framework.core.handler.outbound;

import com.sdu.irpc.framework.common.constant.RpcMessageConstant;
import com.sdu.irpc.framework.common.entity.rpc.RpcResponse;
import com.sdu.irpc.framework.core.compressor.Compressor;
import com.sdu.irpc.framework.core.compressor.CompressorFactory;
import com.sdu.irpc.framework.core.serializer.Serializer;
import com.sdu.irpc.framework.core.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 响应的协议编码处理器
 * 协议编码器
 * 0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22   23  24   25   26   27   28   29   30
 * +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----
 * |    magic          |ver |head  len|    full length    | cd | se |comp|              RequestId                |             Timestamp
 * +-----+-----+-------+----+----+----+----+-----------+--+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----
 * |
 * |                                                   body
 * |
 * +-----------------------------------------------------------------------------------------------------------------------------------------------------
 * 4B magic(魔数)   ---> IrpcClient.getBytes()
 * 1B version(版本) ---> 1
 * 2B header length 头部的长度
 * 4B total  length 报文总长度
 * 1B serialize 序列化方式
 * 1B compression 压缩方式
 * 1B code 响应状态码
 * 8B requestId 请求Id
 * 8B timeStamp 时间戳
 */
@Slf4j
public class ResponseMessageEncoder extends MessageToByteEncoder<RpcResponse> implements RpcMessageConstant {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse, ByteBuf byteBuf) {
        // 4个字节的魔数值
        byteBuf.writeBytes(MAGIC);
        // 1个字节的版本号
        byteBuf.writeByte(VERSION);
        // 2个字节的头部的长度
        byteBuf.writeShort(HEADER_LENGTH);
        // 先把总长度的4字节空出来 先写后面的
        byteBuf.writerIndex(byteBuf.writerIndex() + FULL_FIELD_LENGTH);
        // 请求类型、序列化、压缩类型
        byteBuf.writeByte(rpcResponse.getCode());
        byteBuf.writeByte(rpcResponse.getSerializationType());
        byteBuf.writeByte(rpcResponse.getCompressionType());
        byteBuf.writeLong(rpcResponse.getRequestId());
        byteBuf.writeLong(rpcResponse.getTimeStamp());

        // body
        byte[] body = null;
        int bodyLength = 0;
        if (null != rpcResponse.getBody()) {
            Serializer serializer = SerializerFactory.getSerializer(rpcResponse.getSerializationType()).getImpl();
            body = serializer.serialize(rpcResponse.getBody());
            Compressor compressor = CompressorFactory.getCompressor(rpcResponse.getCompressionType()).getImpl();
            body = compressor.compress(body);
        }
        if (null != body) {
            byteBuf.writeBytes(body);
            bodyLength = body.length;
        }
        int writerIndex = byteBuf.writerIndex();
        // 先回到总长度的位置上
        byteBuf.writerIndex(MAGIC.length + VERSION_LENGTH + HEADER_FIELD_LENGTH);
        // 总长度=(header长度+body长度)
        byteBuf.writeInt(HEADER_LENGTH + bodyLength);
        byteBuf.writerIndex(writerIndex);
        log.debug("请求【{}】已经完成报文的编码。", rpcResponse.getRequestId());
    }
}
