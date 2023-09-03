package com.sdu.irpc.framework.core.serializer;

import com.sdu.irpc.framework.common.entity.ObjectWrapper;
import com.sdu.irpc.framework.common.enums.SerializationType;
import lombok.extern.slf4j.Slf4j;
import com.sdu.irpc.framework.core.serializer.impl.*;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SerializerFactory {

    private final static ConcurrentHashMap<String, ObjectWrapper<Serializer>> SERIALIZER_CACHE = new ConcurrentHashMap<>(8);
    private final static ConcurrentHashMap<Byte, ObjectWrapper<Serializer>> SERIALIZER_CODE_CACHE = new ConcurrentHashMap<>(8);

    static {
        ObjectWrapper<Serializer> jdk = new ObjectWrapper<>((byte) 1, SerializationType.JDK.name(), new JdkSerializer());
        ObjectWrapper<Serializer> json = new ObjectWrapper<>((byte) 2, SerializationType.JSON.name(), new JsonSerializer());
        ObjectWrapper<Serializer> hessian = new ObjectWrapper<>((byte) 3, SerializationType.HESSIAN.name(), new HessianSerializer());
        SERIALIZER_CACHE.put(SerializationType.JDK.name(), jdk);
        SERIALIZER_CACHE.put(SerializationType.JSON.name(), json);
        SERIALIZER_CACHE.put(SerializationType.HESSIAN.name(), hessian);

        SERIALIZER_CODE_CACHE.put((byte) 1, jdk);
        SERIALIZER_CODE_CACHE.put((byte) 2, json);
        SERIALIZER_CODE_CACHE.put((byte) 3, hessian);
    }

    /**
     * 使用工厂方法获取一个SerializerWrapper
     *
     * @param serializeType 序列化的类型
     * @return SerializerWrapper
     */
    public static ObjectWrapper<Serializer> getSerializer(String serializeType) {
        ObjectWrapper<Serializer> serializerWrapper = SERIALIZER_CACHE.get(serializeType);
        if (serializerWrapper == null) {
            log.error("未找到您配置的【{}】序列化工具，默认选用jdk的序列化方式。", serializeType);
            return SERIALIZER_CACHE.get(SerializationType.JDK.name());
        }
        return serializerWrapper;
    }

    public static ObjectWrapper<Serializer> getSerializer(Byte serializeCode) {
        ObjectWrapper<Serializer> serializerWrapper = SERIALIZER_CODE_CACHE.get(serializeCode);
        if (serializerWrapper == null) {
            log.error("未找到您配置的【{}】序列化工具，默认选用jdk的序列化方式。", serializeCode);
            return SERIALIZER_CACHE.get(SerializationType.JDK.name());
        }
        return serializerWrapper;
    }

    /**
     * 新增一个新的序列化器
     *
     * @param serializerObjectWrapper 序列化器的包装
     */
    public static void addSerializer(ObjectWrapper<Serializer> serializerObjectWrapper) {
        SERIALIZER_CACHE.put(serializerObjectWrapper.getName(), serializerObjectWrapper);
        SERIALIZER_CODE_CACHE.put(serializerObjectWrapper.getCode(), serializerObjectWrapper);
    }
}
