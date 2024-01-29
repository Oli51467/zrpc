package com.sdu.zrpc.framework.core.compressor;

import com.sdu.zrpc.framework.common.entity.ObjectWrapper;
import com.sdu.zrpc.framework.common.enums.CompressionType;
import com.sdu.zrpc.framework.core.compressor.impl.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CompressorFactory {

    private final static Map<CompressionType, ObjectWrapper<Compressor>> COMPRESSOR_CACHE = new ConcurrentHashMap<>(8);
    private final static Map<Byte, ObjectWrapper<Compressor>> COMPRESSOR_CODE_CACHE = new ConcurrentHashMap<>(8);

    static {
        ObjectWrapper<Compressor>[] compressors = new ObjectWrapper[]{
                new ObjectWrapper<>((byte) 1, CompressionType.GZIP.name(), new GzipCompressor()),
                new ObjectWrapper<>((byte) 2, CompressionType.DEFLATE.name(), new DeflateCompressor()),
                new ObjectWrapper<>((byte) 3, CompressionType.BZIP2.name(), new Bzip2Compressor()),
                new ObjectWrapper<>((byte) 4, CompressionType.LZO.name(), new LzoCompressor()),
                new ObjectWrapper<>((byte) 5, CompressionType.SNAPPY.name(), new SnappyCompressor()),
                new ObjectWrapper<>((byte) 6, CompressionType.LZ4.name(), new Lz4Compressor())
        };
        for (ObjectWrapper<Compressor> compressor : compressors) {
            COMPRESSOR_CACHE.put(CompressionType.valueOf(compressor.getName()), compressor);
            COMPRESSOR_CODE_CACHE.put(compressor.getCode(), compressor);
        }
    }

    /**
     * 使用工厂方法获取一个CompressorWrapper
     *
     * @param compressorType 序列化的类型
     * @return CompressWrapper
     */
    public static ObjectWrapper<Compressor> getCompressor(CompressionType compressorType) {
        ObjectWrapper<Compressor> compressorObjectWrapper = COMPRESSOR_CACHE.get(compressorType);
        if (compressorObjectWrapper == null) {
            log.error("未找到您配置的【{}】压缩算法，默认选用gzip算法。", compressorType);
            return COMPRESSOR_CACHE.get(CompressionType.GZIP);
        }
        return compressorObjectWrapper;
    }

    public static ObjectWrapper<Compressor> getCompressor(Byte serializeCode) {
        ObjectWrapper<Compressor> compressorObjectWrapper = COMPRESSOR_CODE_CACHE.get(serializeCode);
        if ((compressorObjectWrapper == null)) {
            log.error("未找到您配置的编号为【{}】的压缩算法，默认选用gzip算法。", serializeCode);
            return COMPRESSOR_CACHE.get(CompressionType.GZIP);
        }
        return compressorObjectWrapper;
    }

    /**
     * 给工厂中新增一个压缩方式
     *
     * @param compressorObjectWrapper 压缩类型的包装
     */
    public static void addCompressor(ObjectWrapper<Compressor> compressorObjectWrapper) {
        COMPRESSOR_CODE_CACHE.put(compressorObjectWrapper.getCode(), compressorObjectWrapper);
    }

}
