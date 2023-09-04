package com.sdu.irpc.framework.core.netty;

import com.sdu.irpc.framework.core.handler.ClientChannelHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyBoostrapInitializer {

    private static final Bootstrap bootstrap = new Bootstrap();

    static {
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                // 选择初始化一个什么样的channel
                .channel(NioSocketChannel.class)
                .handler(new ClientChannelHandler());
    }

    private NettyBoostrapInitializer() {}

    public static Bootstrap getBootstrap() { return bootstrap; }
}
