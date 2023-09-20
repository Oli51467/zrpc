package com.sdu.arrow.framework.core.handler.inbound;

import com.sdu.arrow.framework.common.entity.dto.RequestInfo;
import com.sdu.arrow.framework.common.entity.holder.UserContextHolder;
import com.sdu.arrow.framework.common.util.NettyUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.common.StringUtils;

import java.net.InetSocketAddress;

@Slf4j
public class HttpHeadersHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            // 获取请求路径
            HttpHeaders headers = ((FullHttpRequest) msg).headers();
            String ip = headers.get("X-Real-IP");
            // 如果没经过nginx，就直接获取远端地址
            if (StringUtils.isEmpty(ip)) {
                InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                ip = address.getAddress().getHostAddress();
            }
            NettyUtil.setAttr(ctx.channel(), NettyUtil.IP, ip);
            RequestInfo info = new RequestInfo();
            info.setIp(ip);
            UserContextHolder.set(info);
            log.info("Request IP: {}", ip);
            ctx.pipeline().remove(this);
            ctx.fireChannelRead(msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}