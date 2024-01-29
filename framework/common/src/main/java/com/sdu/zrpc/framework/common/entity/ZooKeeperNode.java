package com.sdu.zrpc.framework.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZooKeeperNode {

    private String nodePath;

    private byte[] data;
}
