package com.sdu.arrow.framework.core.loadbalancer.impl;

import com.sdu.arrow.framework.common.entity.rpc.RpcRequest;
import com.sdu.arrow.framework.common.entity.rpc.RpcRequestHolder;
import com.sdu.arrow.framework.core.loadbalancer.AbstractLoadBalancer;
import com.sdu.arrow.framework.core.loadbalancer.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 一致性hash的负载均衡策略
 */
@Slf4j
public class ConsistentHashLoadBalancer extends AbstractLoadBalancer {

    @Override
    protected Selector initSelector(List<InetSocketAddress> serviceList) {
        try {
            return new ConsistentHashSelector(serviceList, 128);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static class ConsistentHashSelector implements Selector {
        private final TreeMap<Integer, InetSocketAddress> circle = new TreeMap<>();
        private final int virtualNodesCount;
        private final MessageDigest messageDigest;

        private ConsistentHashSelector(List<InetSocketAddress> serviceList, int virtualNodesCount) throws NoSuchAlgorithmException {
            // 尝试将节点转化为虚拟节点，进行挂载
            this.virtualNodesCount = virtualNodesCount;
            this.messageDigest = MessageDigest.getInstance("MD5");
            for (InetSocketAddress socketAddress : serviceList) {
                // 需要把每一个节点加入到hash环中
                addNodeToCircle(socketAddress);
            }
        }

        @Override
        public InetSocketAddress select() {
            // 从threadLocal中获取请求的id
            RpcRequest rpcRequest = RpcRequestHolder.get();
            int hash = calHash(rpcRequest.getRequestId().toString());
            // 判断该hash值是否能直接落在一个服务器上
            Map.Entry<Integer, InetSocketAddress> entry = circle.ceilingEntry(hash);
            if (entry == null) {
                entry = circle.firstEntry();
            }
            return entry.getValue();
        }

        /**
         * // 为每一个节点生成匹配的虚拟节点进行挂载
         *
         * @param socketAddress 实际节点的地址
         */
        private void addNodeToCircle(InetSocketAddress socketAddress) {
            for (int i = 0; i < this.virtualNodesCount; i++) {
                int hash = calHash(socketAddress.toString() + "-" + i);
                circle.put(hash, socketAddress);
            }
        }

        private int calHash(String key) {
            byte[] bytes = messageDigest.digest(key.getBytes(StandardCharsets.UTF_8));
            return (bytes[0] & 0xFF) | ((bytes[1] & 0xFF) << 8) | ((bytes[2] & 0xFF) << 16) | ((bytes[3] & 0xFF) << 24);
        }
    }
}
