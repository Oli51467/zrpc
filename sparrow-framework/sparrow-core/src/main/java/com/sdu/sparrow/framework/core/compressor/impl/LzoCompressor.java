package com.sdu.sparrow.framework.core.compressor.impl;

import com.sdu.sparrow.framework.common.exception.CompressException;
import com.sdu.sparrow.framework.core.compressor.Compressor;
import lombok.extern.slf4j.Slf4j;
import org.anarres.lzo.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public class LzoCompressor implements Compressor {

    @Override
    public byte[] compress(byte[] bytes) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             LzoOutputStream lzoOutputStream = new LzoOutputStream(outputStream)) {
            lzoOutputStream.write(bytes);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("对字节数组进行压缩时发生异常", e);
            throw new CompressException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        LzoDecompressor decompressor = LzoLibrary.getInstance().newDecompressor(LzoAlgorithm.LZO1X, null);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
             LzoInputStream lzoInputStream = new LzoInputStream(inputStream, decompressor)) {
            return lzoInputStream.readAllBytes();
        } catch (IOException e) {
            log.error("对字节数组进行压缩时发生异常", e);
            throw new CompressException(e);
        }
    }
}
