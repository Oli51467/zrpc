package com.sdu.sparrow.framework.common.constant;

public interface RpcMessageConstant {

    byte[] MAGIC = "sparrow".getBytes();

    byte VERSION = 1;
    int VERSION_LENGTH = 1;

    // 头部信息的长度
    short HEADER_LENGTH = (byte)(MAGIC.length + 1 + 2 + 4 + 1 + 1 + 1 + 8 + 8);
    // "头部信息长度"占用的字节数
    int HEADER_FIELD_LENGTH = 2;

    int MAX_FRAME_LENGTH = 1024 * 1024;

    // 总长度占用的字节数
    int FULL_FIELD_LENGTH = 4;
}
