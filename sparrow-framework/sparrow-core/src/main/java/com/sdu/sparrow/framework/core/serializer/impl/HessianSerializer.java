package com.sdu.sparrow.framework.core.serializer.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.sdu.sparrow.framework.common.exception.SerializeException;
import com.sdu.sparrow.framework.core.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public class HessianSerializer implements Serializer {

    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
            hessian2Output.writeObject(object);
            hessian2Output.flush();
            byte[] result = byteArrayOutputStream.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("对象【{}】已经完成了序列化操作，序列化后的字节数为【{}】", object, result.length);
            }
            return result;
        } catch (IOException e) {
            log.error("使用hessian进行序列化对象【{}】时放生异常.", object);
            throw new SerializeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || clazz == null) {
            return null;
        }
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            Hessian2Input hessian2Input = new Hessian2Input(byteArrayInputStream);
            T t = (T) hessian2Input.readObject();
            if (log.isDebugEnabled()) {
                log.debug("类【{}】已经使用hessian完成了反序列化操作.", clazz);
            }
            return t;
        } catch (IOException e) {
            log.error("使用hessian进行反序列化对象【{}】时发生异常.", clazz);
            throw new SerializeException(e);
        }
    }
}
