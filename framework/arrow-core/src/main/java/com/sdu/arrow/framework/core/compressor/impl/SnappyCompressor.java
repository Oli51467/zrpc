package com.sdu.arrow.framework.core.compressor.impl;

import com.sdu.arrow.framework.common.exception.CompressException;
import com.sdu.arrow.framework.core.compressor.Compressor;
import lombok.extern.slf4j.Slf4j;
import org.xerial.snappy.Snappy;

import java.io.IOException;

@Slf4j
public class SnappyCompressor implements Compressor {

    @Override
    public byte[] compress(byte[] bytes) {
        try {
            return Snappy.compress(bytes);
        } catch (IOException e) {
            log.error("对字节数组进行压缩时发生异常", e);
            throw new CompressException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        try {
            return Snappy.uncompress(bytes);
        } catch (IOException e) {
            log.error("对字节数组进行压缩时发生异常", e);
            throw new CompressException(e);
        }
    }
}
