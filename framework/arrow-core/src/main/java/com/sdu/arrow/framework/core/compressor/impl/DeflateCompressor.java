package com.sdu.arrow.framework.core.compressor.impl;

import com.sdu.arrow.framework.common.exception.CompressException;
import com.sdu.arrow.framework.core.compressor.Compressor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

@Slf4j
public class DeflateCompressor implements Compressor {

    @Override
    public byte[] compress(byte[] bytes) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream)) {
            deflaterOutputStream.write(bytes);
            deflaterOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("对字节数组进行压缩时发生异常", e);
            throw new CompressException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        Inflater inflater = new Inflater();
        inflater.setInput(bytes);
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             InflaterInputStream inflaterInputStream = new InflaterInputStream(byteArrayInputStream)) {
            return inflaterInputStream.readAllBytes();
        } catch (IOException e) {
            log.error("对字节数组进行压缩时发生异常", e);
            throw new CompressException(e);
        }
    }
}
