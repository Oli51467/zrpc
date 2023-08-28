package com.sdu.irpc;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class NettyTest {

    @Test
    public void testCompositeByteBuf() {
        // 1.组装ByteBuf实现零拷贝
        ByteBuf header = Unpooled.buffer();
        ByteBuf body = Unpooled.buffer();

        CompositeByteBuf byteBuf = Unpooled.compositeBuffer();
        byteBuf.addComponents(header, body);

        // 2.wrap实现零拷贝 包装bytes[] 生成的ByteBuf对象是和bytes数组共用了同一块内存空间
        byte[] buf = new byte[64];
        byte[] buf1 = new byte[64];
        ByteBuf byteBuf1 = Unpooled.wrappedBuffer(buf, buf1);

        ByteBuf buf2 = byteBuf.slice(0, 1);
    }

    @Test
    public void testCompress() throws IOException {
        byte[] buf = new byte[]{12, 23, 43, 34, 35, 54};
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        gzipOutputStream.write(buf);
        gzipOutputStream.finish();

        byte[] bytes = byteArrayOutputStream.toByteArray();
        System.out.println(Arrays.toString(bytes));
    }

    @Test
    public void testDecompress() throws IOException {
        byte[] buf = new byte[]{31, -111, 8, 0, 0, 0, 0, 0, 0, -1, -29, 11, -41, 86, 82, 54, 3, 0, 3, 114, 41, 41, 6, 0, 0, 0};
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buf);
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
        byte[] bytes = gzipInputStream.readAllBytes();

        System.out.println(Arrays.toString(bytes));
    }
}
