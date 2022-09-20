package com.example.mylibrary.message;

public interface IMessageHandler {
    void onMessageReceive(byte[] data);
    byte[] onMessageSend(Message message);
}
