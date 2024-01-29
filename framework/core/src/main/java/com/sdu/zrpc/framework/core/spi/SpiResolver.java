package com.sdu.zrpc.framework.core.spi;

import com.sdu.zrpc.framework.common.entity.ObjectWrapper;
import com.sdu.zrpc.framework.core.compressor.Compressor;
import com.sdu.zrpc.framework.core.compressor.CompressorFactory;
import com.sdu.zrpc.framework.core.config.Configuration;
import com.sdu.zrpc.framework.core.loadbalance.LoadBalance;
import com.sdu.zrpc.framework.core.loadbalance.LoadBalanceFactory;
import com.sdu.zrpc.framework.core.serializer.Serializer;
import com.sdu.zrpc.framework.core.serializer.SerializerFactory;

import java.util.List;

public class SpiResolver {

    /**
     * 通过spi的方式加载配置项
     *
     * @param configuration 配置上下文
     */
    public void loadFromSpi(Configuration configuration) {

        // spi文件中配置了很多实现
        List<ObjectWrapper<LoadBalance>> loadBalanceObjectWrappers = SpiHandler.getList(LoadBalance.class);
        // 将其放入工厂
        if (loadBalanceObjectWrappers != null && loadBalanceObjectWrappers.size() > 0) {
            loadBalanceObjectWrappers.forEach(LoadBalanceFactory::addLoadBalanceStrategy);
        }

        List<ObjectWrapper<Compressor>> compressorObjectWrappers = SpiHandler.getList(Compressor.class);
        if (compressorObjectWrappers != null && compressorObjectWrappers.size() > 0) {
            compressorObjectWrappers.forEach(CompressorFactory::addCompressor);
        }

        List<ObjectWrapper<Serializer>> serializerObjectWrappers = SpiHandler.getList(Serializer.class);
        if (serializerObjectWrappers != null && serializerObjectWrappers.size() > 0) {
            serializerObjectWrappers.forEach(SerializerFactory::addSerializer);
        }
    }
}
