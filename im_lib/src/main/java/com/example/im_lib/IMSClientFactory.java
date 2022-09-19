package com.example.im_lib;

/**
 * ims实例工厂方法
 */
public class IMSClientFactory {
    public static IMSClientInterface getIMSClient() {
        return NettyTcpClient.getInstance();
    }
}
