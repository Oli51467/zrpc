package com.sdu.arrow.framework.core.compressor.impl;

import com.sdu.arrow.framework.common.exception.CompressException;
import com.sdu.arrow.framework.core.compressor.Compressor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public class Bzip2Compressor implements Compressor {

    @Override
    public byte[] compress(byte[] bytes) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             BZip2CompressorOutputStream bZip2OutputStream = new BZip2CompressorOutputStream(out)) {
            bZip2OutputStream.write(bytes);
            bZip2OutputStream.finish();
            return out.toByteArray();
        } catch (IOException e) {
            log.error("对字节数组进行压缩时发生异常", e);
            throw new CompressException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             BZip2CompressorInputStream inputStream = new BZip2CompressorInputStream(byteArrayInputStream)) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            log.error("对字节数组进行压缩时发生异常", e);
            throw new CompressException(e);
        }
    }
}
