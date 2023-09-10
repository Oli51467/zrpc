package com.sdu.irpc.framework.common.util;

import com.sdu.irpc.framework.common.exception.NetworkException;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

@Slf4j
public class NetUtil {

    public static String getIp(Integer port) {
        try {
            // 获取所有的网卡信息
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // 过滤IPv6地址和回环地址
                    if (!addr.isLoopbackAddress() && addr.getAddress().length == 4) {
                        String hostAddress = addr.getHostAddress();
                        // 过滤掉特殊地址
                        if (!hostAddress.startsWith("0.") && !hostAddress.equals("127.0.0.1")) {
                            log.info("局域网IP地址：{}", hostAddress);
                            return hostAddress + ":" + port;
                        }
                    }
                }
            }
            throw new NetworkException();
        } catch (SocketException e) {
            log.error("获取局域网ip时放生异常。", e);
            throw new NetworkException(e);
        }
    }

    public static void main(String[] args) {
        String ip = NetUtil.getIp(8898);
        System.out.println("ip = " + ip);
    }

}
