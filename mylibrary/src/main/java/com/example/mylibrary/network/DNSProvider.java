package com.example.mylibrary.network;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于管理连接的IP
 */
public class DNSProvider {
    private volatile DNSProvider instance;

    private DNSProvider() {
    }

    public DNSProvider getInstance(){
        if (instance == null){
            synchronized (DNSProvider.class){
                if (instance == null){
                    instance = new DNSProvider();
                }
            }
        }
        return instance;
    }
    private final List<DNS> dnsList = new ArrayList<>();

    // TODO: 2022/9/25 对插入的地址进行去重
    public void put(String ip, int port){
        DNS dns = new DNS(ip, port);
        dnsList.add(dns);
    }

    public List<DNS> get(){
        return dnsList;
    }

    private static class DNS{
        private final String ip;
        private final int port;

        public DNS(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        public String getIp() {
            return ip;
        }

        public int getPort() {
            return port;
        }
    }
}
