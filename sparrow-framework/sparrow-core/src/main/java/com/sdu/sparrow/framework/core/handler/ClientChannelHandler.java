package com.sdu.sparrow.framework.core.handler;

import com.sdu.sparrow.framework.core.handler.inbound.ResponseMessageDecoder;
import com.sdu.sparrow.framework.core.handler.inbound.SimpleChannelHandler;
import com.sdu.sparrow.framework.core.handler.outbound.RequestMessageEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ClientChannelHandler extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        socketChannel.pipeline()
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                .addLast(new RequestMessageEncoder())
                .addLast(new ResponseMessageDecoder())
                .addLast(new SimpleChannelHandler());
    }
}
