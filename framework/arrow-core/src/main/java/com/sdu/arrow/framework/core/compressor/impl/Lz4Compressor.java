package com.sdu.arrow.framework.core.compressor.impl;

import com.sdu.arrow.framework.common.exception.CompressException;
import com.sdu.arrow.framework.core.compressor.Compressor;
import lombok.extern.slf4j.Slf4j;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public class Lz4Compressor implements Compressor {
    @Override
    public byte[] compress(byte[] bytes) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             LZ4BlockOutputStream lz4OutputStream = new LZ4BlockOutputStream(outputStream)) {
            lz4OutputStream.write(bytes);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("对字节数组进行压缩时发生异常", e);
            throw new CompressException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
             LZ4BlockInputStream lz4InputStream = new LZ4BlockInputStream(inputStream)) {
            return lz4InputStream.readAllBytes();
        } catch (IOException e) {
            log.error("对字节数组进行压缩时发生异常", e);
            throw new CompressException(e);
        }
    }
}
