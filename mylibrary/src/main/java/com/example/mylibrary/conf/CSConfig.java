package com.example.mylibrary.conf;

/**
 * CS协议通用配置
 */
public class CSConfig {
    //数据包Head
    public static int TCP_FRAME_FIXED_HEADER_LENGTH = 4;     // 4 bytes
    public static int TCP_FRAME_MAX_BODY_LENGTH = 6 * 1024; // 6K bytes
}
