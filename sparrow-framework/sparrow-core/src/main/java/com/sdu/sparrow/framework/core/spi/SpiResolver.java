package com.sdu.sparrow.framework.core.spi;

import com.sdu.sparrow.framework.common.entity.ObjectWrapper;
import com.sdu.sparrow.framework.core.compressor.Compressor;
import com.sdu.sparrow.framework.core.compressor.CompressorFactory;
import com.sdu.sparrow.framework.core.config.Configuration;
import com.sdu.sparrow.framework.core.loadbalancer.LoadBalancer;
import com.sdu.sparrow.framework.core.loadbalancer.LoadBalancerFactory;
import com.sdu.sparrow.framework.core.serializer.Serializer;
import com.sdu.sparrow.framework.core.serializer.SerializerFactory;

import java.util.List;

public class SpiResolver {

    /**
     * 通过spi的方式加载配置项
     *
     * @param configuration 配置上下文
     */
    public void loadFromSpi(Configuration configuration) {

        // 我的spi的文件中配置了很多实现（自由定义，只能配置一个实现，还是多个）
        List<ObjectWrapper<LoadBalancer>> loadBalancerObjectWrappers = SpiHandler.getList(LoadBalancer.class);
        // 将其放入工厂
        if (loadBalancerObjectWrappers != null && loadBalancerObjectWrappers.size() > 0) {
            loadBalancerObjectWrappers.forEach(LoadBalancerFactory::addLoadBalancer);
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
