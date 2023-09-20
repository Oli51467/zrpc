package com.sdu.arrow.framework.core.compressor;

import com.sdu.arrow.framework.common.entity.ObjectWrapper;
import com.sdu.arrow.framework.common.enums.CompressionType;
import com.sdu.arrow.framework.core.compressor.impl.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CompressorFactory {

    private final static Map<CompressionType, ObjectWrapper<Compressor>> COMPRESSOR_CACHE = new ConcurrentHashMap<>(8);
    private final static Map<Byte, ObjectWrapper<Compressor>> COMPRESSOR_CODE_CACHE = new ConcurrentHashMap<>(8);

    static {
        ObjectWrapper<Compressor> gzip = new ObjectWrapper<>((byte) 1, CompressionType.GZIP.name(), new GzipCompressor());
        ObjectWrapper<Compressor> deflate = new ObjectWrapper<>((byte) 2, CompressionType.DEFLATE.name(), new DeflateCompressor());
        ObjectWrapper<Compressor> bzip2 = new ObjectWrapper<>((byte) 3, CompressionType.BZIP2.name(), new Bzip2Compressor());
        ObjectWrapper<Compressor> lzo = new ObjectWrapper<>((byte) 4, CompressionType.LZO.name(), new LzoCompressor());
        ObjectWrapper<Compressor> snappy = new ObjectWrapper<>((byte) 5, CompressionType.SNAPPY.name(), new SnappyCompressor());
        ObjectWrapper<Compressor> lz4 = new ObjectWrapper<>((byte) 6, CompressionType.LZ4.name(), new Lz4Compressor());
        COMPRESSOR_CACHE.put(CompressionType.GZIP, gzip);
        COMPRESSOR_CACHE.put(CompressionType.DEFLATE, deflate);
        COMPRESSOR_CACHE.put(CompressionType.BZIP2, bzip2);
        COMPRESSOR_CACHE.put(CompressionType.LZO, lzo);
        COMPRESSOR_CACHE.put(CompressionType.SNAPPY, snappy);
        COMPRESSOR_CACHE.put(CompressionType.LZ4, lz4);
        COMPRESSOR_CODE_CACHE.put((byte) 1, gzip);
        COMPRESSOR_CODE_CACHE.put((byte) 2, deflate);
        COMPRESSOR_CODE_CACHE.put((byte) 3, bzip2);
        COMPRESSOR_CODE_CACHE.put((byte) 4, lzo);
        COMPRESSOR_CODE_CACHE.put((byte) 5, snappy);
        COMPRESSOR_CODE_CACHE.put((byte) 6, lz4);
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
