package com.example.mylibrary;


import com.example.mylibrary.message.Message;
import com.example.mylibrary.message.MessageDispatcher;

/**
 * 客户端调用消息发送类
 */
public class IMClient {
    public static void send(Message message){
        MessageDispatcher.getInstance().onSend(message);
    }
}