package com.sdu.arrow.framework.core.spi;

import com.sdu.arrow.framework.common.entity.ObjectWrapper;
import com.sdu.arrow.framework.core.compressor.Compressor;
import com.sdu.arrow.framework.core.compressor.CompressorFactory;
import com.sdu.arrow.framework.core.config.Configuration;
import com.sdu.arrow.framework.core.loadbalance.LoadBalance;
import com.sdu.arrow.framework.core.loadbalance.LoadBalanceFactory;
import com.sdu.arrow.framework.core.serializer.Serializer;
import com.sdu.arrow.framework.core.serializer.SerializerFactory;

import java.util.List;

public class SpiResolver {

    /**
     * 通过spi的方式加载配置项
     *
     * @param configuration 配置上下文
     */
    public void loadFromSpi(Configuration configuration) {

        // 我的spi的文件中配置了很多实现（自由定义，只能配置一个实现，还是多个）
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
