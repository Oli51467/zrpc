package com.sdu.arrow.framework.common.util;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public class NettyUtil {

    public static AttributeKey<String> IP = AttributeKey.valueOf("ip");

    public static <T> void setAttr(Channel channel, AttributeKey<T> attributeKey, T data) {
        Attribute<T> attr = channel.attr(attributeKey);
        attr.set(data);
    }

    public static <T> T getAttr(Channel channel, AttributeKey<T> ip) {
        return channel.attr(ip).get();
    }
}
